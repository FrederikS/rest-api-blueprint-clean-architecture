package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
internal abstract class CategoryEntityMapper {

    companion object {
        val INSTANCE: CategoryEntityMapper = Mappers.getMapper(CategoryEntityMapper::class.java)
    }

    fun toEntity(category: Category): CategoryEntity {
        return CategoryEntity(
            category.id(),
            category.name(),
            category.slug(),
            category.parentId(),
            category.visible()
        )
    }

    fun fromEntity(entity: CategoryEntity): Category {
        return Category(
            entity.id,
            entity.name,
            entity.slug,
            entity.parentId,
            entity.visible
        )
    }

}