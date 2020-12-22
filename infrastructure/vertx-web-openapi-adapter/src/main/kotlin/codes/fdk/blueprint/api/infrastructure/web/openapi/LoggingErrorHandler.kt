package codes.fdk.blueprint.api.infrastructure.web.openapi

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.ErrorHandler
import org.slf4j.LoggerFactory

internal class LoggingErrorHandler(private val delegate: ErrorHandler) : Handler<RoutingContext> {

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingErrorHandler::class.java)
    }

    override fun handle(ctx: RoutingContext) {
        delegate.handle(ctx)
        if (ctx.failed()) {
            logger.error(
                "Error while trying to handle request: {} {}",
                ctx.request().method(),
                ctx.request().uri(),
                ctx.failure()
            )
        }
    }
}