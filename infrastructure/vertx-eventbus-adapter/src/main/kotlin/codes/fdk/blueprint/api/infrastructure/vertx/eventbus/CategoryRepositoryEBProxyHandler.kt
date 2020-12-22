package codes.fdk.blueprint.api.infrastructure.vertx.eventbus

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy.Action
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy.Action.FindByParentId
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy.Action.Save
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import reactor.core.publisher.Mono

// TODO use coroutineScope?
class CategoryRepositoryEBProxyHandler(private val delegate: CategoryRepository) : Handler<Message<JsonObject>> {

    override fun handle(message: Message<JsonObject>) {
        when (Action.of(message.headers()["action"])) {
            Save -> save(message)
            FindById -> findById(message)
            FindAll -> findAll(message)
            FindByParentId -> findByParentId(message)
            null -> message.fail(400, "Unknown action.")
        }
    }

    private fun save(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toCategory)
            .let(delegate::save)
            .map(JsonMapper::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findById(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toCategoryId)
            .let(delegate::findById)
            .switchIfEmpty(Mono.empty<Category>().doOnSubscribe { message.fail(404, "Not Found.") })
            .map(JsonMapper::fromCategory)
            .subscribe({ message.reply(it) }, { message.fail(500, it.message) })
    }

    private fun findAll(message: Message<JsonObject>) {
        delegate.findAll()
            .map(JsonMapper::fromCategory)
            .reduce(JsonArray(), { array, category -> array.add(category) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findByParentId(message: Message<JsonObject>) {
        message.body()
            .let(JsonMapper::toCategoryId)
            .let(delegate::findByParentId)
            .map(JsonMapper::fromCategory)
            .reduce(JsonArray(), { array, category -> array.add(category) })
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

}