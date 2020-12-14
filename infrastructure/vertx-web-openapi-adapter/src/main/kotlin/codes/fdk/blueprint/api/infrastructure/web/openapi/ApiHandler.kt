package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.service.CategoryService
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.reactive.asFlow

class ApiHandler(private val categoryService: CategoryService) {

    private val responseMapper: ResponseMapper = ResponseMapper.INSTANCE

    fun rootCategories(): suspend (RoutingContext) -> Unit = { ctx ->
        categoryService.all()
            .map(responseMapper::toResponse)
            .asFlow()
            .fold(JsonArray()) { acc, value -> acc.add(value) }
            .also {
                ctx.response()
                    .setStatusCode(200)
                    .end(it.encode())
            }
    }

}