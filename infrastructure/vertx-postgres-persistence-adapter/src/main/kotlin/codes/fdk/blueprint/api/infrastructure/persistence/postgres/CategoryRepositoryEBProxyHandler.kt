package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindByParentId
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.Save
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageProducer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

internal class CategoryRepositoryEBProxyHandler(
    private val vertx: Vertx,
    private val categoryEntityRepositoryAdapter: CategoryEntityRepositoryAdapter
) : Handler<Message<Any>> {

    override fun handle(message: Message<Any>) {
        when (Action.of(message.headers()["action"])) {
            Save -> save(message)
            FindById -> findById(message)
            FindAll -> findAll(message)
            FindByParentId -> findByParentId(message)
            null -> message.fail(400, "Unknown action.")
        }
    }

    private fun save(message: Message<Any>) {
        categoryEntityRepositoryAdapter
            .save(message.body() as Category)
            .subscribe(message::reply) { message.fail(500, it.message) }
    }

    private fun findById(message: Message<Any>) {
        categoryEntityRepositoryAdapter
            .findById(CategoryId.of(message.body() as String))
            .switchIfEmpty(Mono.empty<Category>().doOnSubscribe { message.fail(404, "Not Found.") })
            .subscribe({ message.reply(it) }, { message.fail(500, it.message) })
    }

    private fun findAll(message: Message<Any>) {
        vertx.eventBus()
            .publisher<Category>("categories-find-all-${UUID.randomUUID()}")
            .streamWhenRecipientReady(message) { categoryEntityRepositoryAdapter.findAll() }
    }

    private fun findByParentId(message: Message<Any>) {
        val parentId = CategoryId.of(message.body() as String)

        vertx.eventBus()
            .publisher<Category>("categories-find-by-parent-id-${UUID.randomUUID()}")
            .streamWhenRecipientReady(message) { categoryEntityRepositoryAdapter.findByParentId(parentId) }
    }

    private fun <T> MessageProducer<T>.streamWhenRecipientReady(message: Message<Any>, source: () -> Flux<T>) {
        message.replyAndRequest<Any>(address())
            .onSuccess {
                source.invoke()
                    .doOnComplete {
                        // TODO use proper signals?
                        write(null)
                        close()
                    }
                    .subscribe({ write(it) }, { message.fail(500, it.message) })
            }.onFailure { message.fail(500, it.message) }
    }

}