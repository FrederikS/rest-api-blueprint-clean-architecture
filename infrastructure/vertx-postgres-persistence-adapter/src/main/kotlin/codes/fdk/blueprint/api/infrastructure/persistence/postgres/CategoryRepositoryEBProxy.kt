package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.FindByParentId
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.CategoryRepositoryEBProxy.Action.Save
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.ReplyException
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class CategoryRepositoryEBProxy(private val vertx: Vertx) : CategoryRepository {

    companion object {
        const val ADDRESS = "category-repository-service"

        private fun action(action: Action) = DeliveryOptions().addHeader("action", action.value)
    }

    override fun save(category: Category): Mono<Category> {
        return mono {
            vertx.eventBus()
                .request<Category>(ADDRESS, category, action(Save))
                .map { it.body() }
                .await()
        }
    }

    override fun findAll(): Flux<Category> {
        return vertx.eventBus().requestStream(ADDRESS, null, action(FindAll))
    }

    override fun findById(id: CategoryId): Mono<Category> {
        return mono {
            try {
                vertx.eventBus()
                    .request<Category>(ADDRESS, id.value(), action(FindById))
                    .map { it.body() }
                    .await()
            } catch (e: ReplyException) {
                if (e.failureCode() != 404) throw e else null
            }
        }
    }

    override fun findByParentId(parentId: CategoryId): Flux<Category> {
        return vertx.eventBus().requestStream(ADDRESS, parentId.value(), action(FindByParentId))
    }

    //TODO measure if custom streaming is less performant then sending lists around
    private fun <T> EventBus.requestStream(address: String, message: Any?, deliveryOptions: DeliveryOptions): Flux<T> {
        return flux {
            val replyWithAddress = request<String>(address, message, deliveryOptions).await()

            val consumer = vertx.eventBus().localConsumer<T>(replyWithAddress.body())
            consumer.completionHandler { replyWithAddress.reply(null) }

            consumer.bodyStream().toChannel(vertx).consumeEach {
                if (it != null) {
                    send(it)
                } else {
                    consumer.unregister()
                }
            }
        }
    }

    internal enum class Action(val value: String) {
        Save("save"),
        FindAll("find-all"),
        FindById("find-by-id"),
        FindByParentId("find-by-parent-id");

        companion object {
            fun of(value: String): Action? {
                return values().firstOrNull { it.value == value }
            }
        }
    }

}