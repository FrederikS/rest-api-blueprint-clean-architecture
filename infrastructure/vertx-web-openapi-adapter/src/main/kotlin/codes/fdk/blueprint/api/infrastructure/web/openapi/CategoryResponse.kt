package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.CategoryId
import com.fasterxml.jackson.annotation.JsonProperty

data class CategoryResponse(
    @get:JsonProperty("id") val id: CategoryId,
    @get:JsonProperty("name") val name: String,
    @get:JsonProperty("slug") val slug: String,
    @get:JsonProperty("parentId") val parentId: CategoryId?,
    @get:JsonProperty("isVisible") val isVisible: Boolean
)