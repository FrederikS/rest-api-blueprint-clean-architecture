package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.CategoryId
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(NON_NULL)
internal data class CategoryResponse(
    @get:JsonProperty("id") val id: CategoryId,
    @get:JsonProperty("name") val name: String,
    @get:JsonProperty("slug") val slug: String,
    @get:JsonProperty("parentId") val parentId: CategoryId?,
    @get:JsonProperty("visible") val visible: Boolean
)