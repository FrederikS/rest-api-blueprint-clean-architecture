package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.infrastructure.persistence.postgres.RandomDataProvider.randomCategory
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.RandomDataProvider.randomCategoryWithId
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.RandomDataProvider.randomChildCategory
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.RandomDataProvider.randomId
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.from
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.PostgreSQLContainer

@ExtendWith(VertxExtension::class)
internal class PostgresCategoryEntityRepositoryTest {

    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:13-alpine").apply {
            start()
        }

        private val pgClient: SqlClient by lazy {
            val connectOptions = PgConnectOptions().apply {
                host = container.containerIpAddress
                port = container.firstMappedPort
                user = container.username
                password = container.password
                database = container.databaseName
            }

            PgPool.pool(connectOptions, PoolOptions())
        }

        private val categoryRepository = PostgresCategoryEntityRepository(pgClient)

        @JvmStatic
        @BeforeAll
        internal fun init() {
            val schema = PostgresCategoryEntityRepository::class.java.getResource("/schema.sql").readText()
            runBlocking { pgClient.query(schema).execute().await() }
        }

    }

    @AfterEach
    internal fun tearDown() {
        runBlocking { pgClient.query("TRUNCATE TABLE ${CategoryEntityRepository.TABLE_NAME};").execute().await() }
    }

    @Nested
    @DisplayName("When findById for non-existent ID")
    internal inner class FindById {

        @Test
        @DisplayName("then null should get returned")
        internal fun shouldReturnEmpty() {
            val findById = runBlocking { categoryRepository.findById(randomId()) }
            assertThat(findById).isNull()
        }

    }

    @Nested
    @DisplayName("When insert a category without id")
    internal inner class InsertCategoryWithoutId {

        @Test
        @DisplayName("then saved category should get returned")
        internal fun shouldReturnSavedCategory() {
            val categoryToSave = randomCategory()

            val savedCategory = runBlocking { categoryRepository.save(categoryToSave) }

            assertThat(savedCategory).isNotNull
                .returns(categoryToSave.name, from(CategoryEntity::name))
                .returns(categoryToSave.slug, from(CategoryEntity::slug))
                .returns(categoryToSave.parentId, from(CategoryEntity::parentId))
                .returns(categoryToSave.isVisible, from(CategoryEntity::isVisible))
        }

        @Test
        @DisplayName("then saved category should get an id assigned")
        internal fun shouldGetAnId() {
            val categoryToSave = randomCategory()
            assertThat(categoryToSave.id).isNull()

            val savedCategory = runBlocking { categoryRepository.save(categoryToSave) }
            assertThat(savedCategory.id).isNotNull
                .extracting { it!!.value() }
                .asString()
                .isNotBlank()
        }

    }

    @Nested
    @DisplayName("When save a category with non-existent id")
    internal inner class InsertWithNonExistentId {

        @Test
        @DisplayName("then save should return an error")
        internal fun shouldReturnAnError() {
            val categoryToSave = randomCategoryWithId()
            assertThrows<IllegalStateException> {
                runBlocking { categoryRepository.save(categoryToSave) }
            }
        }

    }

    @Nested
    @DisplayName("When query all entities on empty repository")
    internal inner class FindAll {

        @Test
        @DisplayName("then nothing should get returned")
        internal fun shouldReturnEmptyPublisher() {
            runBlocking {
                assertThat(categoryRepository.findAll().count()).isZero()
            }
        }

    }

    @Nested
    @DisplayName("When query by non-existent parentId")
    internal inner class FindByParent {

        @Test
        @DisplayName("then nothing should get returned")
        internal fun shouldReturnEmptyPublisher() {
            val entityCountByParentId = runBlocking { categoryRepository.findByParentId(randomId()).count() }
            assertThat(entityCountByParentId).isZero()
        }

    }

    @Nested
    @DisplayName("Given a saved category")
    internal inner class CategoryGiven {

        private lateinit var savedCategory: CategoryEntity

        @BeforeEach
        fun setUp() {
            runBlocking {
                savedCategory = categoryRepository.save(randomCategory())
            }
        }

        @Nested
        @DisplayName("when save for category with id")
        internal inner class Update {

            @Test
            @DisplayName("then saved category should get updated")
            fun shouldReturnCategory() {
                val updatedCategory = savedCategory.copy(name = "new name")
                val savedCategory = runBlocking { categoryRepository.save(updatedCategory) }

                assertThat(savedCategory).isNotNull
                    .returns(updatedCategory.id, from(CategoryEntity::id))
                    .returns(updatedCategory.name, from(CategoryEntity::name))
                    .returns(updatedCategory.slug, from(CategoryEntity::slug))
                    .returns(updatedCategory.parentId, from(CategoryEntity::parentId))
                    .returns(updatedCategory.isVisible, from(CategoryEntity::isVisible))
            }

        }

        @Nested
        @DisplayName("when findById for saved category")
        internal inner class FindById {

            @Test
            @DisplayName("then saved category should get returned")
            fun shouldReturnCategory() {
                val entityById = runBlocking { categoryRepository.findById(savedCategory.id!!) }
                assertThat(entityById).isEqualTo(savedCategory)
            }

        }

        @Nested
        @DisplayName("when findAll")
        internal inner class FindAll {

            @Test
            @DisplayName("then all saved categories should get returned")
            fun shouldReturnAllCategories() {
                runBlocking {
                    val allEntities = categoryRepository.findAll()
                    assertThat(allEntities.count()).isEqualTo(1)
                    assertThat(allEntities.first()).isEqualTo(savedCategory)
                }
            }

        }

    }

    @Nested
    @DisplayName("Given a saved child category")
    internal inner class ChildCategoryGiven {

        private lateinit var savedChildCategory: CategoryEntity

        @BeforeEach
        fun setUp() {
            runBlocking {
                savedChildCategory = categoryRepository.save(randomChildCategory())
            }
        }

        @Nested
        @DisplayName("when query by parentId")
        internal inner class FindByParent {

            @Test
            @DisplayName("then child category with parentId should get returned")
            fun shouldReturnParentIdChild() {
                runBlocking {
                    val entitiesByParentId = categoryRepository.findByParentId(savedChildCategory.parentId!!)
                    assertThat(entitiesByParentId.count()).isEqualTo(1)
                    assertThat(entitiesByParentId.first()).isEqualTo(savedChildCategory)
                }
            }

        }

    }

}