package codes.fdk.blueprint.api.infrastructure.eventbus

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.FindByParentId
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy.Action.Save
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
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
                .request<JsonObject>(ADDRESS, JsonMapper.fromCategory(category), action(Save))
                .await()
                .body()
                .let(JsonMapper::toCategory)
        }
    }

    override fun findAll(): Flux<Category> {
        return flux {
            vertx.eventBus()
                .request<JsonArray>(ADDRESS, null, action(FindAll))
                .await()
                .body()
                .map { it as JsonObject }
                .map(JsonMapper::toCategory)
                .forEach { send(it) }
        }
    }

    override fun findById(id: CategoryId): Mono<Category> {
        return mono {
            try {
                vertx.eventBus()
                    .request<JsonObject>(ADDRESS, JsonMapper.fromCategoryId(id), action(FindById))
                    .await()
                    .body()
                    .let(JsonMapper::toCategory)
            } catch (e: ReplyException) {
                if (e.failureCode() != 404) throw e else null
            }
        }
    }

    override fun findByParentId(parentId: CategoryId): Flux<Category> {
        return flux {
            vertx.eventBus()
                .request<JsonArray>(ADDRESS, JsonMapper.fromCategoryId(parentId), action(FindByParentId))
                .await()
                .body()
                .map { it as JsonObject }
                .map(JsonMapper::toCategory)
                .forEach { send(it) }
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