package codes.fdk.blueprint.api.infrastructure.web.openapi

import com.fasterxml.jackson.annotation.JsonProperty

data class PostCategoryRequest(
    @JsonProperty("name") val name: String,
    @JsonProperty("slug") val slug: String,
    @JsonProperty("visible") val visible: Boolean
)
