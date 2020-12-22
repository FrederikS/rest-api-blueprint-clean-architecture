package codes.fdk.blueprint.api.infrastructure.eventbus

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindByParentId
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.Save
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.json.JsonObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

// TODO use coroutineScope?
class CategoryRepositoryEBProxyHandler(
    private val vertx: Vertx,
    private val delegate: CategoryRepository
) : Handler<Message<JsonObject>> {

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
        delegate
            .save(JsonMapper.toCategory(message.body()))
            .map(JsonMapper::fromCategory)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findById(message: Message<JsonObject>) {
        delegate
            .findById(JsonMapper.toCategoryId(message.body()))
            .map(JsonMapper::fromCategory)
            .switchIfEmpty(Mono.empty<JsonObject>().doOnSubscribe { message.fail(404, "Not Found.") })
            .subscribe({ message.reply(it) }, { message.fail(500, it.message) })
    }

    private fun findAll(message: Message<JsonObject>) {
        vertx.eventBus()
            .publisher<JsonObject>("categories-find-all-${UUID.randomUUID()}")
            .streamWhenRecipientReady(message) { delegate.findAll() }
    }

    private fun findByParentId(message: Message<JsonObject>) {
        val parentId = JsonMapper.toCategoryId(message.body())

        vertx.eventBus()
            .publisher<JsonObject>("categories-find-by-parent-id-${UUID.randomUUID()}")
            .streamWhenRecipientReady(message) { delegate.findByParentId(parentId) }
    }

    private fun MessageProducer<JsonObject>.streamWhenRecipientReady(
        message: Message<JsonObject>,
        source: () -> Flux<Category>
    ) {
        message.replyAndRequest<Any>(address())
            .onSuccess {
                source.invoke()
                    .doOnComplete {
                        // TODO use proper signals?
                        write(null)
                        close()
                    }
                    .subscribe({ write(JsonMapper.fromCategory(it)) }, { message.fail(500, it.message) })
            }.onFailure { message.fail(500, it.message) }
    }

}