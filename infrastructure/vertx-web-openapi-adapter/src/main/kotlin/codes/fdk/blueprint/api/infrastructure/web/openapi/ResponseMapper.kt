package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.Category

object ResponseMapper {

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