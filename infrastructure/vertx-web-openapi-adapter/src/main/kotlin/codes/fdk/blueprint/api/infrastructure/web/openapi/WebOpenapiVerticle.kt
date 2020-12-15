package codes.fdk.blueprint.api.infrastructure.web.openapi

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.LoggerHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.openapi.Operation
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class WebOpenapiVerticle : CoroutineVerticle() {

    private lateinit var apiHandler: ApiHandler

    companion object {
        private val LOG = LoggerFactory.getLogger(WebOpenapiVerticle::class.java)
    }

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)

        apiHandler = ApiHandler(CategoryServiceEBProxy(vertx))
    }

    override suspend fun start() {
        super.start()

        val router = Router.router(vertx)
        router.route().handler(LoggerHandler.create())
        router.route("/").handler { it.redirect("/swagger-ui/index.html?url=/api-spec") }
        router.route("/api-spec").handler(StaticHandler.create("openapi.json"))
        router.route("/swagger-ui/*").handler(StaticHandler.create("META-INF/resources/webjars/swagger-ui/3.37.2"))

        val api = RouterBuilder.create(vertx, "openapi.json").await()
        api.operation("getRootCategories").coroutineHandler(apiHandler.rootCategories())
        api.operation("postCategory").coroutineHandler(apiHandler.postCategory())

        router.mountSubRouter("/", api.createRouter())

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onSuccess { LOG.info("Running on port: ${it.actualPort()}") }
            .await()
    }

    private fun Operation.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Operation {
        return handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }

}