package codes.fdk.blueprint.api.infrastructure.web.openapi

import com.fasterxml.jackson.annotation.JsonProperty

//TODO rename property to isVisible -> visible?
data class PostCategoryRequest(
    @JsonProperty("name") val name: String,
    @JsonProperty("slug") val slug: String,
    @JsonProperty("visible") val isVisible: Boolean
)
