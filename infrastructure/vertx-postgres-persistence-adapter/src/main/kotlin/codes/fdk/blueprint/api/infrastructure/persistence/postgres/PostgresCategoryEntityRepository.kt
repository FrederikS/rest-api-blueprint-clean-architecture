package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.CategoryId
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import java.util.Objects
import java.util.UUID

internal class PostgresCategoryEntityRepository(private val pgClient: SqlClient) : CategoryEntityRepository {

    companion object {
        private val LOG = LoggerFactory.getLogger(PostgresCategoryEntityRepository::class.java)

        private val INSERT_STATEMENT = """
            INSERT INTO ${CategoryEntityRepository.TABLE_NAME}
            VALUES (DEFAULT, $1, $2, $3, $4)            
            RETURNING *;
        """.trimIndent()

        private val UPDATE_STATEMENT = """
            UPDATE ${CategoryEntityRepository.TABLE_NAME}
            SET name       = $2,
                slug       = $3,
                parent_id  = $4,
                is_visible = $5
            WHERE id = $1
            RETURNING *;
        """.trimIndent()

        private val categoryEntityMapper: (Row) -> CategoryEntity = { row ->
            CategoryEntity(
                id = fromUUID(row.getUUID("id")),
                name = row.getString("name"),
                slug = row.getString("slug"),
                parentId = fromUUID(row.getUUID("parent_id")),
                isVisible = row.getBoolean("is_visible")
            )
        }

        private fun fromUUID(uuid: UUID?): CategoryId? {
            return uuid?.let { CategoryId.of(uuid.toString()) }
        }

        private fun toUUID(id: CategoryId?): UUID? {
            return id?.let { UUID.fromString(id.value()) }
        }
    }

    override suspend fun save(entity: CategoryEntity): CategoryEntity {
        return when (entity.id) {
            null -> insert(entity)
            else -> update(entity)
        }
    }

    override suspend fun findAll(): Flow<CategoryEntity> {
        return pgClient.preparedQuery("SELECT * FROM ${CategoryEntityRepository.TABLE_NAME};")
            .mapping(categoryEntityMapper)
            .execute()
            .onFailure { LOG.error("Error while trying to select all entities.", it) }
            .await()
            .toFlow()
    }

    override suspend fun findById(id: CategoryId): CategoryEntity? {
        Objects.requireNonNull(id, "id must not be null.")

        return pgClient.preparedQuery("SELECT * FROM ${CategoryEntityRepository.TABLE_NAME} WHERE id = $1;")
            .mapping(categoryEntityMapper)
            .execute(Tuple.of(toUUID(id)))
            .onFailure { LOG.error("Error while trying to select entity by id:{}", id, it) }
            .map { it.firstOrNull() }
            .await()
    }

    override suspend fun findByParentId(parentId: CategoryId): Flow<CategoryEntity> {
        Objects.requireNonNull(parentId, "parentId must not be null.")

        return pgClient.preparedQuery("SELECT * FROM ${CategoryEntityRepository.TABLE_NAME} WHERE parent_id = $1;")
            .mapping(categoryEntityMapper)
            .execute(Tuple.of(toUUID(parentId)))
            .onFailure { LOG.error("Error while trying to select entities by parentId:{}", parentId, it) }
            .map { it.toFlow() }
            .await()
    }

    private suspend fun insert(entity: CategoryEntity): CategoryEntity {
        return pgClient.preparedQuery(INSERT_STATEMENT)
            .mapping(categoryEntityMapper)
            .execute(Tuple.of(entity.name, entity.slug, toUUID(entity.parentId), entity.isVisible))
            .onFailure { LOG.error("Error while trying to insert entity {}", entity, it) }
            .map { it.first() }
            .await()
    }

    private suspend fun update(entity: CategoryEntity): CategoryEntity {
        Objects.requireNonNull(entity.id, "id must not be null.")

        return pgClient.preparedQuery(UPDATE_STATEMENT)
            .mapping(categoryEntityMapper)
            .execute(Tuple.of(toUUID(entity.id), entity.name, entity.slug, toUUID(entity.parentId), entity.isVisible))
            .onFailure { LOG.error("Error while trying to update entity {}", entity, it) }
            .map {
                when (it.rowCount() == 1) {
                    true -> it.first()
                    false -> throw IllegalStateException("Nothing updated. No entity with id:${entity.id}.")
                }
            }
            .await()
    }

    //TODO use row-stream?
    private fun <T> RowSet<T>.toFlow(): Flow<T> {
        return flow<T> {
            emitAll(asFlow())
            while (next() != null) {
                emitAll(next().asFlow())
            }
        }
    }

}