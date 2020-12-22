package codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres

import codes.fdk.blueprint.api.domain.model.CategoryId

internal data class CategoryEntity(
    val id: CategoryId?,
    val name: String,
    val slug: String,
    val parentId: CategoryId?,
    val visible: Boolean
)
