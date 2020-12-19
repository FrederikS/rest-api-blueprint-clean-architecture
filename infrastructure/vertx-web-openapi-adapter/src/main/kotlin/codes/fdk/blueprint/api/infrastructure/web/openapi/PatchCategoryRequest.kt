package codes.fdk.blueprint.api.infrastructure.web.openapi

import com.fasterxml.jackson.annotation.JsonProperty

data class PatchCategoryRequest(@JsonProperty("visible") val visible: Boolean)
