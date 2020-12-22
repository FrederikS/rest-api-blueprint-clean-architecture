package codes.fdk.blueprint.api.infrastructure.vertx.eventbus

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.service.CategoryService
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy.Action.Create
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy.Action.FindAll
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy.Action.FindById
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy.Action.FindChildren
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy.Action.Update
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

class CategoryServiceEBProxy(private val vertx: Vertx) : CategoryService {

    companion object {
        const val ADDRESS = "category-service"

        private fun action(action: Action) = DeliveryOptions().addHeader("action", action.value)
    }

    override fun create(command: CreateCategoryCommand): Mono<Category> {
        return mono {
            vertx.eventBus()
                .request<JsonObject>(ADDRESS, JsonMapper.fromCreateCategoryCommand(command), action(Create))
                .await()
                .body()
                .let(JsonMapper::toCategory)
        }
    }

    override fun update(command: UpdateCategoryCommand): Mono<Category> {
        return mono {
            vertx.eventBus()
                .request<JsonObject>(ADDRESS, JsonMapper.fromUpdateCategoryCommand(command), action(Update))
                .await()
                .body()
                .let(JsonMapper::toCategory)
        }
    }

    override fun byId(id: CategoryId): Mono<Category> {
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

    override fun all(): Flux<Category> {
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

    override fun children(parentId: CategoryId): Flux<Category> {
        return flux {
            vertx.eventBus()
                .request<JsonArray>(ADDRESS, JsonMapper.fromCategoryId(parentId), action(FindChildren))
                .await()
                .body()
                .map { it as JsonObject }
                .map(JsonMapper::toCategory)
                .forEach { send(it) }
        }
    }

    enum class Action(val value: String) {
        Create("create"),
        Update("update"),
        FindById("find-by-id"),
        FindAll("find-all"),
        FindChildren("find-children");

        companion object {
            fun of(value: String): Action? {
                return values().firstOrNull { it.value == value }
            }
        }

    }

}