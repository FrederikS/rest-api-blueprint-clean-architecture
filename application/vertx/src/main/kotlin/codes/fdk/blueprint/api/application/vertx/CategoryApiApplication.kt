package codes.fdk.blueprint.api.application.vertx

import codes.fdk.blueprint.api.domain.service.CategoryService
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxy
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryServiceEBProxyHandler
import codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres.PostgresPersistenceVerticle
import codes.fdk.blueprint.api.infrastructure.vertx.web.openapi.WebOpenapiVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.CompositeFuture
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import kotlin.system.exitProcess

private val log = LoggerFactory.getLogger("CategoryApiApplication")

//TODO health-check
fun main() {
    val vertx = Vertx.vertx()
    val categoryService = CategoryService.create(CategoryRepositoryEBProxy(vertx))

    vertx.eventBus()
        .localConsumer<JsonObject>(CategoryServiceEBProxy.ADDRESS)
        .handler(CategoryServiceEBProxyHandler(categoryService))

    ConfigRetriever.create(vertx)
        .config
        .compose {
            val deploymentOptions = DeploymentOptions().setConfig(it)
            CompositeFuture.all(
                vertx.deployVerticle(WebOpenapiVerticle(), deploymentOptions),
                vertx.deployVerticle(PostgresPersistenceVerticle(), deploymentOptions),
            )
        }
        .onSuccess { log.info("Deployment successful!") }
        .onFailure {
            log.error("Deployment failed!", it)
            exitProcess(-1)
        }
}
