package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.service.CategoryService
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http.HttpHeaders.LOCATION
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle

class ApiHandler(private val categoryService: CategoryService) {

    fun rootCategories(): suspend (RoutingContext) -> Unit = { ctx ->
        categoryService.all()
            .map(ResponseMapper::toResponse)
            .asFlow()
            .fold(JsonArray()) { acc, value -> acc.add(value) }
            .also {
                ctx.response()
                    .setStatusCode(200)
                    .putHeader(CONTENT_TYPE, "application/json")
                    .end(it.encode())
            }
    }

    fun postCategory(): suspend (RoutingContext) -> Unit = { ctx ->
        ctx.bodyAsJson
            .mapTo(PostCategoryRequest::class.java)
            .let(CommandMapper::toCommand)
            .let(categoryService::create)
            .awaitSingle()
            .let(ResponseMapper::toResponse)
            .also {
                ctx.response()
                    .setStatusCode(201)
                    .putHeader(LOCATION, "/categories/${it.id}")
                    .end()
            }
    }

}