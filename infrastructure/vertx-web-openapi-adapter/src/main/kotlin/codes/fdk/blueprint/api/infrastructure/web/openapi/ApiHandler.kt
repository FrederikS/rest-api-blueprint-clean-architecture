package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.service.CategoryService
import io.vertx.core.http.HttpHeaders.CONTENT_LOCATION
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http.HttpHeaders.LOCATION
import io.vertx.core.json.Json
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
            .also {
                ctx.response()
                    .setStatusCode(201)
                    .putHeader(LOCATION, "/categories/${it.id()}")
                    .end()
            }
    }

    fun getCategory(): suspend (RoutingContext) -> Unit = { ctx ->
        categoryService.byId(CategoryId.of(ctx.pathParam("id")))
            .map(ResponseMapper::toResponse)
            .awaitSingle()
            .also {
                ctx.response()
                    .setStatusCode(200)
                    .putHeader(CONTENT_TYPE, "application/json")
                    .end(Json.encode(it))
            }
    }

    fun updateCategory(): suspend (RoutingContext) -> Unit = { ctx ->
        ctx.bodyAsJson
            .mapTo(PatchCategoryRequest::class.java)
            .let { CommandMapper.toCommand(CategoryId.of(ctx.pathParam("id")), it) }
            .let(categoryService::update)
            .awaitSingle()
            .also {
                ctx.response()
                    .setStatusCode(204)
                    .putHeader(CONTENT_LOCATION, "/categories/${it.id()}")
                    .end()
            }
    }

}