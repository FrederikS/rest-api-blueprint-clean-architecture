package codes.fdk.blueprint.api.infrastructure.vertx.web.openapi

import com.fasterxml.jackson.annotation.JsonProperty

internal data class PatchCategoryRequest(@JsonProperty("visible") val visible: Boolean)
