package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.service.CategoryService
import codes.fdk.blueprint.api.domain.stub.InMemoryCategoryRepository
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action.Create
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action.FindChildren
import codes.fdk.blueprint.api.infrastructure.web.openapi.CategoryServiceEBProxy.Action.Update
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import reactor.core.publisher.Mono

class CategoryServiceEBProxyHandler(private val vertx: Vertx) : Handler<Message<JsonObject>> {

    private val categoryService = CategoryService.create(InMemoryCategoryRepository())

    override fun handle(message: Message<JsonObject>) {
        when (Action.of(message.headers()["action"])) {
            Create -> createCategory(message)
            Update -> updateCategory(message)
            FindById -> findCategoryById(message)
            FindAll -> findAllCategories(message)
            FindChildren -> findChildCategories(message)
            null -> message.fail(400, "Unknown action.")
        }
    }

    private fun createCategory(message: Message<JsonObject>) {
        message.body()
            .mapToCreateCategoryCommand()
            .let(categoryService::create)
            .map(Companion::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun updateCategory(message: Message<JsonObject>) {
        message.body()
            .mapTo(UpdateCategoryCommand::class.java)
            .let(categoryService::update)
            .map(JsonObject::mapFrom)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findCategoryById(message: Message<JsonObject>) {
        message.body()
            .mapTo(CategoryId::class.java)
            .let(categoryService::byId)
            .switchIfEmpty(Mono.empty<Category>().doOnSubscribe { message.fail(404, "Not Found.") })
            .map(JsonObject::mapFrom)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findAllCategories(message: Message<JsonObject>) {
        categoryService.all()
            .reduce(JsonArray(), { acc, value -> acc.add(value) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findChildCategories(message: Message<JsonObject>) {
        message.body()
            .mapTo(CategoryId::class.java)
            .let(categoryService::children)
            .reduce(JsonArray(), { acc, value -> acc.add(value) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun JsonObject.mapToCreateCategoryCommand(): CreateCategoryCommand {
        return CreateCategoryCommand(
            getString("name"),
            getString("slug"),
            getString("parentId")?.let { CategoryId.of(it) },
            getBoolean("visible")
        )
    }

    companion object {

        private fun fromCategory(category: Category): JsonObject {
            return JsonObject(mapOf(
                "id" to Json.encode(category.id()),
                "name" to category.name(),
                "slug" to category.slug(),
                "parentId" to Json.encode(category.parentId()),
                "visible" to category.visible()
            ))
        }

    }

}