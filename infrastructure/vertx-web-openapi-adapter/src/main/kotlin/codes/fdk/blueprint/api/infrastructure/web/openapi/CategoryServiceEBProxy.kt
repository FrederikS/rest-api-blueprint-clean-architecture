package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.service.CategoryService
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class CategoryServiceEBProxy(private val vertx: Vertx) : CategoryService {

    companion object {
        const val ADDRESS = "category-service"

        private fun action(action: Action) = DeliveryOptions().addHeader("action", action.value)
    }

    override fun create(command: CreateCategoryCommand): Mono<Category> {
        TODO("Not yet implemented")
    }

    override fun update(command: UpdateCategoryCommand): Mono<Category> {
        TODO("Not yet implemented")
    }

    override fun byId(id: CategoryId): Mono<Category> {
        TODO("Not yet implemented")
    }

    override fun all(): Flux<Category> {
        return Flux.empty()
    }

    override fun children(parentId: CategoryId): Flux<Category> {
        TODO("Not yet implemented")
    }

    enum class Action(val value: String) {
        Create("create"),
        Update("update"),
        ById("by-id"),
        All("all"),
        Children("children")
    }

}