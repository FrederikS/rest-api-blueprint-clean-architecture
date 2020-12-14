package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.service.CategoryService
import io.vertx.ext.web.RoutingContext

class ApiHandler(private val categoryService: CategoryService) {

    fun rootCategories(): (RoutingContext) -> Unit = {
        it.response().setStatusCode(200).end("[]")
    }

}