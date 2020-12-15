package codes.fdk.blueprint.api.infrastructure.web.openapi

import com.fasterxml.jackson.annotation.JsonProperty

data class PostCategoryRequest(
    @JsonProperty("name") val name: String,
    @JsonProperty("slug") val slug: String,
    @JsonProperty("isVisible") val isVisible: Boolean
)
