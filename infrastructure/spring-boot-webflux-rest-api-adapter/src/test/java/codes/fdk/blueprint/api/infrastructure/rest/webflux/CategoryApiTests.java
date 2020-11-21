package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.CategoryWebTestClient.PostCategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.hateoas.UriTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@IntegrationTest
@AutoConfigureWebTestClient
class CategoryApiTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CategoryWebTestClient categoryWebTestClient;

    @Nested
    @DisplayName("When GET /categories")
    class GetRootCategories {

        @Test
        @DisplayName("then status code 200 should get returned")
        void shouldReturn200() {
            webTestClient.get().uri("/categories")
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectStatus().isOk();
        }

        @Test
        @DisplayName("then json content type should get returned")
        void shouldReturnJson() {
            webTestClient.get().uri("/categories")
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectHeader().contentType(APPLICATION_JSON);
        }

        @Test
        @DisplayName("then an empty category list should get returned")
        void shouldReturnEmptyList() {
            webTestClient.get().uri("/categories")
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectBody()
                         .jsonPath("$.content").isArray()
                         .jsonPath("$.content").isEmpty();
        }

        @Test
        @DisplayName("then json response should contain self link")
        void shouldContainSelfLink() {
            webTestClient.get().uri("/categories")
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectBody()
                         .jsonPath("$.links").isArray()
                         .jsonPath("$.links").isNotEmpty()
                         .jsonPath("$.links[?(@.rel == 'self')].href").isEqualTo("/categories");
        }

    }

    @Nested
    @DisplayName("Given a root category")
    class RootCategoryGiven {

        private PostCategoryRequest rootCategoryRequest;
        private PostCategoryResponse rootCategoryResponse;

        @BeforeEach
        void setUp() {
            rootCategoryRequest = RandomDataProvider.randomPostCategoryRequest();
            rootCategoryResponse = categoryWebTestClient.postCategory(rootCategoryRequest).getResponseBody();
        }

        @Nested
        @DisplayName("when GET /categories")
        class GetRootCategories {

            @Test
            @DisplayName("then status code 200 should get returned")
            void shouldReturn200() {
                webTestClient.get().uri("/categories")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isOk();
            }

            @Test
            @DisplayName("then json content type should get returned")
            void shouldReturnJson() {
                webTestClient.get().uri("/categories")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectHeader().contentType(APPLICATION_JSON);
            }

            @Test
            @DisplayName("then the root category in list should contain data from post-category-request")
            void shouldReturnRootCategoryData() {
                webTestClient.get().uri("/categories")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content.length()").isEqualTo(1)
                             .jsonPath("$.content[0].name").isEqualTo(rootCategoryRequest.name())
                             .jsonPath("$.content[0].slug").isEqualTo(rootCategoryRequest.slug())
                             .jsonPath("$.content[0].parentId").doesNotExist()
                             .jsonPath("$.content[0].isVisible").isEqualTo(rootCategoryRequest.isVisible());
            }

            @Test
            @DisplayName("then the root category in list should contain self link")
            void shouldContainSelfLinkInChildrenData() {
                webTestClient.get()
                             .uri("/categories")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'self')].href")
                             .isEqualTo(categoryHrefFor(rootCategoryResponse.id()));
            }

            @Test
            @DisplayName("then the root category in list should contain children link")
            void shouldContainChildrenLinkInChildrenData() {
                webTestClient.get().uri("/categories")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'children')].href")
                             .isEqualTo(childrenCategoryHrefFor(rootCategoryResponse.id()));
            }

        }

        @Nested
        @DisplayName("when GET /categories/{id}")
        class GetCategory {

            @Test
            @DisplayName("then status code 200 should get returned")
            void shouldReturn200() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isOk();
            }

            @Test
            @DisplayName("then json content type should get returned")
            void shouldReturnJson() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectHeader().contentType(APPLICATION_JSON);
            }

            @Test
            @DisplayName("then an etag header should get returned")
            void shouldReturnEtag() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectHeader().value(ETAG, v -> assertThat(v).isNotBlank());
            }

            @Test
            @DisplayName("then data of post-category-request should be included in json response")
            void shouldReturnCategoryRequestData() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.name").isEqualTo(rootCategoryRequest.name())
                             .jsonPath("$.slug").isEqualTo(rootCategoryRequest.slug())
                             .jsonPath("$.parentId").doesNotExist()
                             .jsonPath("$.isVisible").isEqualTo(rootCategoryRequest.isVisible());
            }

            @Test
            @DisplayName("then a non-blank id should be included in json response")
            void shouldReturnAnId() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.id").value(c -> assertThat(c).asString().isNotBlank());
            }

            @Test
            @DisplayName("then a categories link should be included in json response")
            void shouldContainCategoriesLink() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.links").isArray()
                             .jsonPath("$.links").isNotEmpty()
                             .jsonPath("$.links[?(@.rel == 'categories')].href").isEqualTo("/categories");
            }

            @Test
            @DisplayName("then a self link should be included in json response")
            void shouldContainSelfLink() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.links").isArray()
                             .jsonPath("$.links").isNotEmpty()
                             .jsonPath("$.links[?(@.rel == 'self')].href")
                             .isEqualTo(categoryHrefFor(rootCategoryResponse.id()));
            }

            @Test
            @DisplayName("then a children link should be included in json response")
            void shouldContainChildrenLink() {
                webTestClient.get().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.links").isArray()
                             .jsonPath("$.links").isNotEmpty()
                             .jsonPath("$.links[?(@.rel == 'children')].href")
                             .isEqualTo(childrenCategoryHrefFor(rootCategoryResponse.id()));
            }

        }

        @Nested
        @DisplayName("when PATCH /categories/{id}")
        class PatchCategory {

            @Test
            @DisplayName("then status code 204 should get returned")
            void shouldReturn200() {
                var patchCategoryRequest = new PatchCategoryRequest(!rootCategoryRequest.isVisible());

                webTestClient.patch()
                             .uri(categoryHrefFor(rootCategoryResponse.id()))
                             .bodyValue(patchCategoryRequest)
                             .exchange()
                             .expectStatus().isNoContent();
            }

            @Test
            @DisplayName("then content-location header to patched category should get returned")
            void shouldReturnLocationHeaderWithCategoryURLofPatchedItem() {
                var patchCategoryRequest = new PatchCategoryRequest(!rootCategoryRequest.isVisible());

                webTestClient.patch()
                             .uri(categoryHrefFor(rootCategoryResponse.id()))
                             .bodyValue(patchCategoryRequest)
                             .exchange()
                             .expectHeader()
                             .valueEquals(CONTENT_LOCATION, categoryHrefFor(rootCategoryResponse.id()));
            }

            @Test
            @DisplayName("then categories visibility should get updated")
            void patchCategory() {
                var patchCategoryRequest = new PatchCategoryRequest(!rootCategoryRequest.isVisible());

                var patchResponse = webTestClient.patch().uri(categoryHrefFor(rootCategoryResponse.id()))
                                                 .bodyValue(patchCategoryRequest)
                                                 .exchange()
                                                 .expectBody()
                                                 .returnResult();

                webTestClient.get().uri(patchResponse.getResponseHeaders().getFirst(CONTENT_LOCATION))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isOk()
                             .expectBody()
                             .jsonPath("$.isVisible").isEqualTo(patchCategoryRequest.isVisible());
            }

        }

        @Nested
        @DisplayName("when POST /categories/{id}")
        class PostChildCategory {

            @Test
            @DisplayName("then 201 status code should get returned")
            void shouldReturn201() {
                webTestClient.post().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .contentType(APPLICATION_JSON)
                             .bodyValue(RandomDataProvider.randomPostCategoryRequest())
                             .exchange()
                             .expectStatus().isCreated();
            }

            @Test
            @DisplayName("then location header to created category should get returned")
            void shouldReturnLocationHeader() {
                webTestClient.post().uri(categoryHrefFor(rootCategoryResponse.id()))
                             .contentType(APPLICATION_JSON)
                             .bodyValue(RandomDataProvider.randomPostCategoryRequest())
                             .exchange()
                             .expectHeader().value(LOCATION, v -> assertThat(v).matches("^\\/categories\\/[^\s\\/]+$"));
            }

        }

    }

    @Nested
    @DisplayName("Given a root category and child category")
    class RootAndChildCategoryGiven {

        private PostCategoryResponse rootCategoryResponse;
        private PostCategoryRequest childCategoryRequest;
        private PostCategoryResponse childCategoryResponse;

        @BeforeEach
        void setUp() {
            rootCategoryResponse = categoryWebTestClient.postCategory(RandomDataProvider.randomPostCategoryRequest()).getResponseBody();

            childCategoryRequest = RandomDataProvider.randomPostCategoryRequest();
            childCategoryResponse = categoryWebTestClient.postChildCategory(childCategoryRequest, rootCategoryResponse.id())
                                                         .getResponseBody();
        }

        @Nested
        @DisplayName("when GET /categories/{id} for child category")
        class GetChildCategory {

            @Test
            @DisplayName("then a parentId should be included in json response")
            void shouldContainParentId() {
                webTestClient.get().uri(categoryHrefFor(childCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.parentId").isEqualTo(rootCategoryResponse.id().value());
            }

            @Test
            @DisplayName("then a parent link should be included in json response")
            void shouldContainParentLink() {
                webTestClient.get().uri(categoryHrefFor(childCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.links").isArray()
                             .jsonPath("$.links").isNotEmpty()
                             .jsonPath("$.links[?(@.rel == 'parent')].href")
                             .isEqualTo(categoryHrefFor(rootCategoryResponse.id()));
            }

        }

        @Nested
        @DisplayName("when GET /categories/{id}/children")
        class GetCategoryChildren {

            @Test
            @DisplayName("then status code 200 should get returned")
            void shouldReturn200() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isOk();
            }

            @Test
            @DisplayName("then json content type should get returned")
            void shouldReturnJson() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectHeader().contentType(APPLICATION_JSON);
            }

            @Test
            @DisplayName("then json response should contain self link")
            void shouldContainSelfLink() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.links").isArray()
                             .jsonPath("$.links").isNotEmpty()
                             .jsonPath("$.links[?(@.rel == 'self')].href")
                             .isEqualTo(childrenCategoryHrefFor(rootCategoryResponse.id()));
            }

            @Test
            @DisplayName("then a list of children categories should get returned")
            void shouldReturnChildrenList() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content").isArray()
                             .jsonPath("$.content").isNotEmpty()
                             .jsonPath("$.content.length()").isEqualTo(1);
            }

            @Test
            @DisplayName("then the children category in list should contain data from post-category-request")
            void shouldReturnChildrenData() {
                webTestClient.get()
                             .uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].name").isEqualTo(childCategoryRequest.name())
                             .jsonPath("$.content[0].slug").isEqualTo(childCategoryRequest.slug())
                             .jsonPath("$.content[0].parentId")
                             .isEqualTo(rootCategoryResponse.id().value())
                             .jsonPath("$.content[0].isVisible").isEqualTo(childCategoryRequest.isVisible());
            }

            @Test
            @DisplayName("then the children category in list should contain categories link")
            void shouldContainCategoriesLinkInChildrenData() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'categories')].href").isEqualTo("/categories");
            }

            @Test
            @DisplayName("then the children category in list should contain self link")
            void shouldContainSelfLinkInChildrenData() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'self')].href")
                             .isEqualTo(categoryHrefFor(childCategoryResponse.id()));
            }

            @Test
            @DisplayName("then the children category in list should contain children link")
            void shouldContainChildrenLinkInChildrenData() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'children')].href")
                             .isEqualTo(childrenCategoryHrefFor(childCategoryResponse.id()));
            }

            @Test
            @DisplayName("then the children category in list should contain parent link")
            void shouldContainParentLinkInChildrenData() {
                webTestClient.get().uri(childrenCategoryHrefFor(rootCategoryResponse.id()))
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$.content[0].links").isArray()
                             .jsonPath("$.content[0].links").isNotEmpty()
                             .jsonPath("$.content[0].links[?(@.rel == 'parent')].href")
                             .isEqualTo(categoryHrefFor(rootCategoryResponse.id()));
            }

        }

    }

    @Nested
    @DisplayName("When POST /categories")
    class PostCategory {

        @Test
        @DisplayName("then 201 status code should get returned")
        void shouldReturn201() {
            webTestClient.post().uri("/categories")
                         .contentType(APPLICATION_JSON)
                         .bodyValue(RandomDataProvider.randomPostCategoryRequest())
                         .exchange()
                         .expectStatus().isCreated();
        }

        @Test
        @DisplayName("then location header to created category should get returned")
        void shouldReturnLocationHeader() {
            webTestClient.post().uri("/categories")
                         .contentType(APPLICATION_JSON)
                         .bodyValue(RandomDataProvider.randomPostCategoryRequest())
                         .exchange()
                         .expectHeader().value(LOCATION, v -> assertThat(v).matches("^\\/categories\\/[^\s\\/]+$"));
        }

    }

    @Nested
    @DisplayName("When GET /categories/{id} for non-existent id")
    class GetNotExistentCategory {

        @Test
        @DisplayName("then status code 404 should get returned")
        void shouldReturn404() {
            webTestClient.get().uri("/categories/{id}", RandomDataProvider.randomUUID())
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectStatus().isNotFound();
        }

    }

    @Nested
    @DisplayName("When GET /categories/{id}/children for non-existent id")
    class GetChildrenForNotExistentId {

        @Test
        @DisplayName("then status code 404 should get returned")
        void shouldReturn404() {
            webTestClient.get().uri("/categories/{id}/children", RandomDataProvider.randomUUID())
                         .accept(APPLICATION_JSON)
                         .exchange()
                         .expectStatus().isNotFound();
        }

    }

    @Nested
    @DisplayName("When POST /categories/{id} for non-existent id")
    class PostChildCategory {

        @Test
        @DisplayName("then status code 404 should get returned")
        void shouldReturn404() {
            webTestClient.post().uri("/categories/{id}", RandomDataProvider.randomUUID())
                         .contentType(APPLICATION_JSON)
                         .bodyValue(RandomDataProvider.randomPostCategoryRequest())
                         .exchange()
                         .expectStatus().isNotFound();
        }

    }

    @Nested
    @DisplayName("Given a root category and valid etag header")
    class ValidEtag {

        private String eTag;
        private CategoryId categoryId;

        @BeforeEach
        void setUp() {
            var rootCategoryResponse = categoryWebTestClient.postCategory(RandomDataProvider.randomPostCategoryRequest());
            eTag = rootCategoryResponse.getResponseHeaders().getETag();
            categoryId = rootCategoryResponse.getResponseBody().id();
        }

        @Nested
        @DisplayName("when GET /categories/{id}")
        class GetCategory {

            @Test
            @DisplayName("then status code 304 should get returned")
            void shouldReturn304() {
                webTestClient.get().uri(categoryHrefFor(categoryId))
                             .header(IF_NONE_MATCH, eTag)
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isNotModified();
            }

            @Test
            @DisplayName("then eTag Header should get returned")
            void shouldReturnETagHeaderAgain() {
                webTestClient.get().uri(categoryHrefFor(categoryId))
                             .header(IF_NONE_MATCH, eTag)
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectHeader().valueEquals(ETAG, eTag);
            }


            @Test
            @DisplayName("then no body should get returned")
            void shouldReturnEmptyBody() {
                webTestClient.get().uri(categoryHrefFor(categoryId))
                             .header(IF_NONE_MATCH, eTag)
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody().isEmpty();
            }

        }

    }

    @Nested
    @DisplayName("Given a root category and an expired etag header")
    class ExpiredEtag {

        private CategoryId categoryId;

        @BeforeEach
        void setUp() {
            var rootCategoryResponse = categoryWebTestClient.postCategory(RandomDataProvider.randomPostCategoryRequest());
            categoryId = rootCategoryResponse.getResponseBody().id();
        }

        @Nested
        @DisplayName("when GET /categories/{id}")
        class GetCategory {

            @Test
            @DisplayName("then status code 200 should get returned")
            void shouldReturn200() {
                webTestClient.get().uri(categoryHrefFor(categoryId))
                             .header(IF_NONE_MATCH, "expired-etag")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectStatus().isOk();
            }

            @Test
            @DisplayName("then a body should get returned")
            void shouldHaveABody() {
                webTestClient.get().uri(categoryHrefFor(categoryId))
                             .header(IF_NONE_MATCH, "expired-etag")
                             .accept(APPLICATION_JSON)
                             .exchange()
                             .expectBody()
                             .jsonPath("$").isNotEmpty();
            }

        }

    }

    private static String categoryHrefFor(CategoryId id) {
        return UriTemplate.of("/categories/{id}").expand(id).toString();
    }

    private static String childrenCategoryHrefFor(CategoryId id) {
        return UriTemplate.of("/categories/{id}/children").expand(id).toString();
    }

}
