package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.CategoryId
import kotlinx.coroutines.flow.Flow

interface CategoryEntityRepository {

    companion object {
        const val TABLE_NAME = "categories"
    }

    suspend fun save(entity: CategoryEntity): CategoryEntity
    suspend fun findAll(): Flow<CategoryEntity>
    suspend fun findById(id: CategoryId): CategoryEntity?
    suspend fun findByParentId(parentId: CategoryId): Flow<CategoryEntity>

}