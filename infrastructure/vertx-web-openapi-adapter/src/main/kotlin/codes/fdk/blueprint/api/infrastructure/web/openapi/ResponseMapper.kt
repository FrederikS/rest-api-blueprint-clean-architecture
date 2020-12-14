package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.Category
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
abstract class ResponseMapper {

    companion object {
        val INSTANCE: ResponseMapper = Mappers.getMapper(ResponseMapper::class.java)
    }

    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id(),
            name = category.name(),
            slug = category.slug(),
            parentId = category.parentId(),
            isVisible = category.isVisible
        )
    }

}