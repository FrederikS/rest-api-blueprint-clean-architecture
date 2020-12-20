package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.stub.ResetInMemoryRepoExtension
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders.ACCEPT
import io.vertx.core.http.HttpHeaders.CONTENT_LOCATION
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http.HttpHeaders.ETAG
import io.vertx.core.http.HttpHeaders.IF_NONE_MATCH
import io.vertx.core.http.HttpHeaders.LOCATION
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
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
                .onFailure { context.failNow(it) }
                .onComplete {
                    context.verify {
                        rootCategoryLocation = it.result().getHeader(LOCATION.toString())
                        assertThat(rootCategoryLocation).isNotBlank()
                        context.completeNow()
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

        @Nested
        @DisplayName("when PATCH /categories/{id}")
        internal inner class PatchCategory {

            @Test
            @DisplayName("then status code 204 should get returned")
            fun shouldReturn204(context: VertxTestContext) {
                val patchRequest = PatchCategoryRequest(!rootCategoryRequest.visible)

                webClient.patch(rootCategoryLocation)
                    .putHeader(CONTENT_TYPE.toString(), "application/json")
                    .sendJson(patchRequest)
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(204)
                    }
            }

            @Test
            @DisplayName("then content-location header to patched category should get returned")
            fun shouldReturnLocationHeaderWithCategoryURLofPatchedItem(context: VertxTestContext) {
                val patchRequest = PatchCategoryRequest(!rootCategoryRequest.visible)

                webClient.patch(rootCategoryLocation)
                    .putHeader(CONTENT_TYPE.toString(), "application/json")
                    .sendJson(patchRequest)
                    .assertThat(context) {
                        assertThat(it.getHeader(CONTENT_LOCATION.toString())).isEqualTo(rootCategoryLocation)
                    }
            }

            @Test
            @DisplayName("then categories visibility should get updated")
            fun patchCategory(context: VertxTestContext) {
                val patchRequest = PatchCategoryRequest(!rootCategoryRequest.visible)

                runBlocking {
                    webClient.patch(rootCategoryLocation)
                        .putHeader(CONTENT_TYPE.toString(), "application/json")
                        .sendJson(patchRequest)
                        .await()
                }

                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            inPath("$.visible").isEqualTo(patchRequest.visible)
                        }
                    }
            }

        }

        @Nested
        @DisplayName("when POST /categories/{id}")
        internal inner class PostChildCategory {

            @Test
            @DisplayName("then 201 status code should get returned")
            fun shouldReturn201(context: VertxTestContext) {
                webClient.post(rootCategoryLocation)
                    .putHeader(CONTENT_TYPE.toString(), "application/json")
                    .sendJson(RandomDataProvider.randomPostCategoryRequest())
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(201)
                    }
            }

            @Test
            @DisplayName("then location header to created category should get returned")
            fun shouldReturnLocationHeader(context: VertxTestContext) {
                webClient.post(rootCategoryLocation)
                    .putHeader(CONTENT_TYPE.toString(), "application/json")
                    .sendJson(RandomDataProvider.randomPostCategoryRequest())
                    .assertThat(context) {
                        assertThat(it.getHeader(LOCATION.toString())).matches("^\\/categories\\/[^\\s\\/]+$")
                    }
            }

        }

    }

    @Nested
    @DisplayName("Given a root category and child category")
    internal inner class RootAndChildCategoryGiven {

        private lateinit var rootCategoryLocation: String
        private lateinit var childCategoryLocation: String
        private lateinit var childCategoryRequest: PostCategoryRequest

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            childCategoryRequest = RandomDataProvider.randomPostCategoryRequest()

            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .compose {
                    rootCategoryLocation = it.getHeader(LOCATION.toString())
                    webClient.post(it.getHeader(LOCATION.toString()))
                        .putHeader(CONTENT_TYPE.toString(), "application/json")
                        .sendJson(childCategoryRequest)
                }
                .onFailure { context.failNow(it) }
                .onComplete {
                    context.verify {
                        childCategoryLocation = it.result().getHeader(LOCATION.toString())
                        assertThat(childCategoryLocation).isNotBlank()
                        context.completeNow()
                    }
                }
        }

        @Nested
        @DisplayName("when GET /categories/{id} for child category")
        internal inner class GetChildCategory {

            @Test
            @DisplayName("then a parentId should be included in json response")
            fun shouldContainParentId(context: VertxTestContext) {
                webClient.get(childCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            inPath("$.parentId").asString().matches { id -> rootCategoryLocation.endsWith(id) }
                        }
                    }
            }

        }

        @Nested
        @DisplayName("when GET /categories/{id}/children")
        internal inner class GetCategoryChildren {

            @Test
            @DisplayName("then status code 200 should get returned")
            fun shouldReturn200(context: VertxTestContext) {
                webClient.get("${rootCategoryLocation}/children")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(200)
                    }
            }

            @Test
            @DisplayName("then json content type should get returned")
            fun shouldReturnJson(context: VertxTestContext) {
                webClient.get("${rootCategoryLocation}/children")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThat(it.getHeader(CONTENT_TYPE.toString())).isEqualTo("application/json")
                    }
            }

            @Test
            @DisplayName("then a list of children categories should get returned")
            fun shouldReturnChildrenList(context: VertxTestContext) {
                webClient.get("${rootCategoryLocation}/children")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            isArray.isNotEmpty.hasSize(1)
                        }
                    }
            }

            @Test
            @DisplayName("then the children category in list should contain data from post-category-request")
            fun shouldReturnChildrenData(context: VertxTestContext) {
                webClient.get("${rootCategoryLocation}/children")
                    .putHeader(ACCEPT.toString(), "application/json")
                    .send()
                    .assertThat(context) {
                        assertThatJson(it.bodyAsString()) {
                            inPath("$[0].name").isEqualTo(childCategoryRequest.name)
                            inPath("$[0].slug").isEqualTo(childCategoryRequest.slug)
                            inPath("$[0].visible").isEqualTo(childCategoryRequest.visible)
                            inPath("$[0].parentId").asString().matches { id -> rootCategoryLocation.endsWith(id) }
                        }
                    }
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
    @DisplayName("When GET /categories/{id} for non-existent id")
    internal inner class GetNotExistentCategory {

        @Test
        @DisplayName("then status code 404 should get returned")
        fun shouldReturn404(context: VertxTestContext) {
            webClient.get("/categories/non-existent-id")
                .putHeader(ACCEPT.toString(), "application/json")
                .send()
                .assertThat(context) {
                    assertThat(it.statusCode()).isEqualTo(404)
                }
        }

    }

    @Nested
    @DisplayName("When GET /categories/{id}/children for non-existent id")
    internal inner class GetChildrenForNotExistentId {

        @Test
        @DisplayName("then status code 404 should get returned")
        fun shouldReturn404(context: VertxTestContext) {
            webClient.get("/categories/non-existent-id/children")
                .putHeader(ACCEPT.toString(), "application/json")
                .send()
                .assertThat(context) {
                    assertThat(it.statusCode()).isEqualTo(404)
                }
        }

    }

    @Nested
    @DisplayName("When POST /categories/{id} for non-existent id")
    internal inner class PostChildCategory {

        @Test
        @DisplayName("then status code 404 should get returned")
        fun shouldReturn404(context: VertxTestContext) {
            webClient.post("/categories/non-existent-id")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .assertThat(context) {
                    assertThat(it.statusCode()).isEqualTo(404)
                }
        }

    }

    @Nested
    @DisplayName("Given a root category and valid etag header")
    internal inner class ValidEtag {

        private lateinit var eTag: String
        private lateinit var rootCategoryLocation: String

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .compose {
                    rootCategoryLocation = it.getHeader(LOCATION.toString())
                    webClient.get(rootCategoryLocation)
                        .putHeader(ACCEPT.toString(), "application/json")
                        .send()
                }
                .onFailure { context.failNow(it) }
                .onComplete {
                    context.verify {
                        eTag = it.result().getHeader(ETAG.toString())
                        assertThat(eTag).isNotBlank()

                        context.completeNow()
                    }
                }
        }

        @Nested
        @DisplayName("when GET /categories/{id}")
        internal inner class GetCategory {

            @Test
            @DisplayName("then status code 304 should get returned")
            fun shouldReturn304(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    //TODO remove comma when bug is fixed
                    .putHeader(IF_NONE_MATCH.toString(), "${eTag},")
                    .send()
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(304)
                    }
            }

            @Test
            @DisplayName("then eTag Header should get returned")
            fun shouldReturnETagHeaderAgain(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    //TODO remove comma when bug is fixed
                    .putHeader(IF_NONE_MATCH.toString(), "${eTag},")
                    .send()
                    .assertThat(context) {
                        assertThat(it.getHeader(ETAG.toString())).isEqualTo(eTag)
                    }
            }

            @Test
            @DisplayName("then no body should get returned")
            fun shouldReturnEmptyBody(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    //TODO remove comma when bug is fixed
                    .putHeader(IF_NONE_MATCH.toString(), "${eTag},")
                    .send()
                    .assertThat(context) {
                        assertThat(it.body()).isNull()
                    }
            }

        }
    }

    @Nested
    @DisplayName("Given a root category and an expired etag header")
    internal inner class ExpiredEtag {

        private lateinit var rootCategoryLocation: String

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            webClient.post("/categories")
                .putHeader(CONTENT_TYPE.toString(), "application/json")
                .sendJson(RandomDataProvider.randomPostCategoryRequest())
                .onFailure { context.failNow(it) }
                .onComplete {
                    context.verify {
                        rootCategoryLocation = it.result().getHeader(LOCATION.toString())
                        assertThat(rootCategoryLocation).isNotBlank()
                        context.completeNow()
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
                    .putHeader(IF_NONE_MATCH.toString(), "expired-etag")
                    .send()
                    .assertThat(context) {
                        assertThat(it.statusCode()).isEqualTo(200)
                    }
            }

            @Test
            @DisplayName("then a body should get returned")
            fun shouldHaveABody(context: VertxTestContext) {
                webClient.get(rootCategoryLocation)
                    .putHeader(ACCEPT.toString(), "application/json")
                    .putHeader(IF_NONE_MATCH.toString(), "expired-etag")
                    .send()
                    .assertThat(context) {
                        assertThat(it.body()).isNotNull
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