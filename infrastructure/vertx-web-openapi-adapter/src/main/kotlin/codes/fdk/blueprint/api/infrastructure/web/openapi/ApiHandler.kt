package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.model.Category
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
import java.security.MessageDigest
import java.util.NoSuchElementException

internal class ApiHandler(private val categoryService: CategoryService) {

    companion object {
        private fun categoryLocationFor(category: Category): String = "/categories/${category.id()}"

        private fun md5DigestAsHex(byteArray: ByteArray): String {
            return MessageDigest.getInstance("MD5")
                .digest(byteArray)
                .joinToString("") { "%02x".format(it) }
        }
    }

    val rootCategories: suspend (RoutingContext) -> Unit = { ctx ->
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

    val postCategory: suspend (RoutingContext) -> Unit = { ctx ->
        ctx.bodyAsJson
            .mapTo(PostCategoryRequest::class.java)
            .let(CommandMapper::toCreateCommand)
            .let(categoryService::create)
            .awaitSingle()
            .also {
                ctx.response()
                    .setStatusCode(201)
                    .putHeader(LOCATION, categoryLocationFor(it))
                    .end()
            }
    }

    val getCategory: suspend (RoutingContext) -> Unit = { ctx ->
        try {
            categoryService.byId(CategoryId.of(ctx.pathParam("id")))
                .map(ResponseMapper::toResponse)
                .awaitSingle()
                .also {
                    ctx.etag(md5DigestAsHex(it.hashCode().toString().toByteArray()))

                    if (ctx.isFresh) {
                        ctx.response()
                            .setStatusCode(304)
                            .end()
                    } else {
                        ctx.response()
                            .setStatusCode(200)
                            .putHeader(CONTENT_TYPE, "application/json")
                            .end(Json.encode(it))
                    }
                }
        } catch (e: NoSuchElementException) {
            ctx.response()
                .setStatusCode(404)
                .end()
        }
    }

    val updateCategory: suspend (RoutingContext) -> Unit = { ctx ->
        ctx.bodyAsJson
            .mapTo(PatchCategoryRequest::class.java)
            .let { CommandMapper.toUpdateCommand(CategoryId.of(ctx.pathParam("id")), it) }
            .let(categoryService::update)
            .awaitSingle()
            .also {
                ctx.response()
                    .setStatusCode(204)
                    .putHeader(CONTENT_LOCATION, categoryLocationFor(it))
                    .end()
            }
    }

    val postChildCategory: suspend (RoutingContext) -> Unit = { ctx ->
        val parentId = CategoryId.of(ctx.pathParam("id"))

        try {
            categoryService.byId(parentId).awaitSingle()
            ctx.bodyAsJson
                .mapTo(PostCategoryRequest::class.java)
                .let { CommandMapper.toCreateCommand(parentId, it) }
                .let(categoryService::create)
                .awaitSingle()
                .also {
                    ctx.response()
                        .setStatusCode(201)
                        .putHeader(LOCATION, categoryLocationFor(it))
                        .end()
                }
        } catch (e: NoSuchElementException) {
            ctx.response()
                .setStatusCode(404)
                .end()
        }
    }

    val getChildCategories: suspend (RoutingContext) -> Unit = { ctx ->
        val parentId = CategoryId.of(ctx.pathParam("id"))

        try {
            categoryService.byId(parentId).awaitSingle()
            categoryService.children(parentId)
                .map(ResponseMapper::toResponse)
                .asFlow()
                .fold(JsonArray()) { acc, value -> acc.add(value) }
                .also {
                    ctx.response()
                        .setStatusCode(200)
                        .putHeader(CONTENT_TYPE, "application/json")
                        .end(it.encode())
                }
        } catch (e: NoSuchElementException) {
            ctx.response()
                .setStatusCode(404)
                .end()
        }
    }

}