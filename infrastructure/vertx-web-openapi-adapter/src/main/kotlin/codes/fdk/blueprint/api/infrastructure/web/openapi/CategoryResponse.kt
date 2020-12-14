package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.CategoryId
import com.fasterxml.jackson.annotation.JsonProperty

//TODO json property name not working?
data class CategoryResponse(
    @JsonProperty("id") val id: CategoryId,
    @JsonProperty("name") val name: String,
    @JsonProperty("slug") val slug: String,
    @JsonProperty("parentId") val parentId: CategoryId?,
    @JsonProperty("isVisible") val isVisible: Boolean
)