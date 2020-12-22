package codes.fdk.blueprint.api.infrastructure.eventbus

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.service.CategoryService
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action.Create
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action.FindChildren
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy.Action.Update
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import reactor.core.publisher.Mono

class CategoryServiceEBProxyHandler(private val delegate: CategoryService) : Handler<Message<JsonObject>> {

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
            .let(JsonMapper::toCreateCategoryCommand)
            .let(delegate::create)
            .map(JsonMapper::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun updateCategory(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toUpdateCategoryCommand)
            .let(delegate::update)
            .map(JsonMapper::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findCategoryById(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toCategoryId)
            .let(delegate::byId)
            .switchIfEmpty(Mono.empty<Category>().doOnSubscribe { message.fail(404, "Not Found.") })
            .map(JsonMapper::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findAllCategories(message: Message<JsonObject>) {
        delegate.all()
            .map(JsonMapper::fromCategory)
            .reduce(JsonArray(), { array, category -> array.add(category) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findChildCategories(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toCategoryId)
            .let(delegate::children)
            .map(JsonMapper::fromCategory)
            .reduce(JsonArray(), { array, category -> array.add(category) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

}