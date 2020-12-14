package codes.fdk.blueprint.api.infrastructure.web.openapi

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders.ACCEPT
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class WebOpenapiVerticleTest {

    companion object {
        private lateinit var webClient: WebClient

        @JvmStatic
        @BeforeAll
        fun init(vertx: Vertx, context: VertxTestContext) {
            webClient = WebClient.create(vertx, WebClientOptions().apply {
                defaultHost = "localhost"
                defaultPort = 8080
            })

            vertx.deployVerticle(WebOpenapiVerticle())
                .onComplete(context.succeedingThenComplete())
                .onFailure { context.failNow(it) }
        }
    }

    @Nested
    @DisplayName("When GET /categories")
    internal inner class GetRootCategories {

        @Test
        @DisplayName("then status code 200 should get returned")
        internal fun shouldReturn200(context: VertxTestContext) {
            webClient.get("/categories")
                .putHeader(ACCEPT.toString(), "application/json")
                .send()
                .assertThat(context) {
                    assertThat(it.statusCode()).isEqualTo(200)
                }
        }

        @Test
        @DisplayName("then json content type should get returned")
        internal fun shouldReturnJson(context: VertxTestContext) {
            webClient.get("/categories")
                .putHeader(ACCEPT.toString(), "application/json")
                .send()
                .assertThat(context) {
                    assertThat(it.getHeader(CONTENT_TYPE.toString())).isEqualTo("application/json")
                }
        }

        @Test
        @DisplayName("then an empty category list should get returned")
        internal fun shouldReturnEmptyList(context: VertxTestContext) {
            webClient.get("/categories")
                .putHeader(ACCEPT.toString(), "application/json")
                .send()
                .assertThat(context) {
                    assertThat(it.bodyAsString()).isEqualTo("[]")
                }
        }

    }

    private fun <T> Future<HttpResponse<T>>.assertThat(context: VertxTestContext, verify: (HttpResponse<T>) -> Unit) {
        onSuccess {
            context.verify {
                verify(it)
                context.completeNow()
            }
        }
        onFailure { context.failNow(it) }
    }

}