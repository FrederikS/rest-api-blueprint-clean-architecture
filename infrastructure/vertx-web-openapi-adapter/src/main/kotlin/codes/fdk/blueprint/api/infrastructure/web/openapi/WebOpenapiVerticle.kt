package codes.fdk.blueprint.api.infrastructure.web.openapi

import io.vertx.ext.web.handler.LoggerHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory

class WebOpenapiVerticle : CoroutineVerticle() {

    companion object {
        private val LOG = LoggerFactory.getLogger(WebOpenapiVerticle::class.java)
    }

    override suspend fun start() {
        super.start()

        val router = RouterBuilder.create(vertx, "openapi.json").await()
            .rootHandler(LoggerHandler.create())
            .createRouter()

        router.route("/api-spec").handler(StaticHandler.create("openapi.json"))
        router.route("/swagger-ui/*").handler(StaticHandler.create("META-INF/resources/webjars/swagger-ui/3.37.2"))
        router.route().handler { it.redirect("/swagger-ui/index.html?url=/api-spec") }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onSuccess { LOG.info("Running on port: ${it.actualPort()}") }
            .await()
    }

}