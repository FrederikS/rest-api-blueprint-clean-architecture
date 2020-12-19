package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.stub.ResetInMemoryRepoExtension
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders.ACCEPT
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http.HttpHeaders.ETAG
import io.vertx.core.http.HttpHeaders.LOCATION
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class, ResetInMemoryRepoExtension::class)
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

            vertx.eventBus()
                .localConsumer<JsonObject>(CategoryServiceEBProxy.ADDRESS)
                .handler(CategoryServiceEBProxyHandler())

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

    @Nested
    @DisplayName("When POST /categories")
    internal inner class PostCategory {

        @Test
        @DisplayName("then 201 status code should get returned")
        fun shouldReturn201(context: VertxTestContext) {
            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .assertThat(context) {
                    assertThat(it.statusCode()).isEqualTo(201)
                }
        }

        @Test
        @DisplayName("then location header to created category should get returned")
        fun shouldReturnLocationHeader(context: VertxTestContext) {
            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .assertThat(context) {
                    assertThat(it.getHeader(LOCATION.toString())).matches("^\\/categories\\/[^\\s\\/]+$")
                }
        }
    }

    @Nested
    @DisplayName("Given a root category")
    internal inner class RootCategoryGiven {

        private lateinit var rootCategoryRequest: PostCategoryRequest
        private lateinit var rootCategoryLocation: String

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            rootCategoryRequest = RandomDataProvider.randomPostCategoryRequest()

            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(rootCategoryRequest)
                .onSuccess { context.completeNow() }
                .onFailure { context.failNow(it) }
                .onComplete {
                    context.verify {
                        rootCategoryLocation = it.result().getHeader(LOCATION.toString())
                        assertThat(rootCategoryLocation).isNotBlank()
                    }
                }
        }


        @Nested
        @DisplayName("when GET /categories")
        internal inner class GetRootCategories {

            @Test
            @DisplayName("then status code 200 should get returned")
            fun shouldReturn200(context: VertxTestContext) {
                webClient.get("/categories")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(200)
                    }
            }

            @Test
            @DisplayName("then json content type should get returned")
            fun shouldReturnJson(context: VertxTestContext) {
                webClient.get("/categories")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.getHeader(CONTENT_TYPE.toString())).isEqualTo("application/json")
                    }
            }

            @Test
            @DisplayName("then the root category in list should contain data from post-category-request")
            fun shouldReturnRootCategoryData(context: VertxTestContext) {
                webClient.get("/categories")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            isArray.hasSize(1)
                            inPath("$[0].name").isEqualTo(rootCategoryRequest.name)
                            inPath("$[0].slug").isEqualTo(rootCategoryRequest.slug)
                            inPath("$[0].parentId").isAbsent()
                            inPath("$[0].visible").isEqualTo(rootCategoryRequest.visible)
                        }
                    }
            }

        }

        @Nested
        @DisplayName("when GET /categories/{id}")
        internal inner class GetCategory {

            @Test
            @DisplayName("then status code 200 should get returned")
            fun shouldReturn200(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(200)
                    }
            }

            @Test
            @DisplayName("then json content type should get returned")
            fun shouldReturnJson(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.getHeader(CONTENT_TYPE.toString())).isEqualTo("application/json")
                    }
            }

            @Test
            @DisplayName("then an etag header should get returned")
            fun shouldReturnEtag(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.getHeader(ETAG.toString())).isNotBlank()
                    }
            }

            @Test
            @DisplayName("then data of post-category-request should be included in json response")
            fun shouldReturnCategoryRequestData(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            inPath("$.name").isEqualTo(rootCategoryRequest.name)
                            inPath("$.slug").isEqualTo(rootCategoryRequest.slug)
                            inPath("$.parentId").isAbsent()
                            inPath("$.visible").isEqualTo(rootCategoryRequest.visible)
                        }
                    }
            }

            @Test
            @DisplayName("then a non-blank id should be included in json response")
            fun shouldReturnAnId(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            inPath("$.id").isString.isNotBlank
                        }
                    }
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