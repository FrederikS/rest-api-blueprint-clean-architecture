package codes.fdk.blueprint.api.application.vertx

import codes.fdk.blueprint.api.domain.service.CategoryService
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryRepositoryEBProxy
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxy
import codes.fdk.blueprint.api.infrastructure.eventbus.CategoryServiceEBProxyHandler
import codes.fdk.blueprint.api.infrastructure.persistence.postgres.PostgresPersistenceVerticle
import codes.fdk.blueprint.api.infrastructure.web.openapi.WebOpenapiVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.CompositeFuture
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject

private val log = LoggerFactory.getLogger("CategoryApiApplication")

fun main() {
    val vertx = Vertx.vertx()
    val categoryService = CategoryService.create(CategoryRepositoryEBProxy(vertx))

    vertx.eventBus()
        .localConsumer<JsonObject>(CategoryServiceEBProxy.ADDRESS)
        .handler(CategoryServiceEBProxyHandler(categoryService))

    ConfigRetriever.create(vertx, ConfigRetrieverOptions())
        .config
        .compose {
            val deploymentOptions = DeploymentOptions().setConfig(it)
            CompositeFuture.all(
                vertx.deployVerticle(WebOpenapiVerticle(), deploymentOptions),
                vertx.deployVerticle(PostgresPersistenceVerticle(), deploymentOptions),
            )
        }
        .onFailure { log.error("Deployment failed!", it) }
        .onSuccess { log.info("Deployment successful!") }
}
