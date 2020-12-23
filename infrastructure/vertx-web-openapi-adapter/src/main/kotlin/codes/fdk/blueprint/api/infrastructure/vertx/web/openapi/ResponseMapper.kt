package codes.fdk.blueprint.api.infrastructure.vertx.web.openapi

import codes.fdk.blueprint.api.domain.model.Category

internal object ResponseMapper {

    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id(),
            name = category.name(),
            slug = category.slug(),
            parentId = category.parentId(),
            visible = category.visible()
        )
    }

}