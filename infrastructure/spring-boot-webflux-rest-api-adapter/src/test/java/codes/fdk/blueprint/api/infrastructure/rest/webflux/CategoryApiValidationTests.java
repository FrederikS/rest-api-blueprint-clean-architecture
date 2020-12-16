package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.infrastructure.rest.webflux.CategoryWebTestClient.PostCategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@IntegrationTest
@AutoConfigureWebTestClient
public class CategoryApiValidationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CategoryWebTestClient categoryWebTestClient;

    @Nested
    @DisplayName("When POST /categories with invalid name")
    class PostCategoryWithInvalidName {

        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        @ParameterizedTest(name = "name = \"{0}\"")
        @DisplayName("then status code 400 should get returned")
        void shouldReturn400(String name) {
            webTestClient.post().uri("/categories")
                         .contentType(APPLICATION_JSON)
                         .bodyValue(RandomDataProvider.randomPostCategoryRequestWithName(name))
                         .exchange()
                         .expectStatus().isBadRequest();
        }

    }

    @Nested
    @DisplayName("When POST /categories with invalid slug")
    class PostCategoryWithInvalidSlug {

        @NullSource
        @EmptySource
        @ValueSource(strings = {" ", "slug containing whitespaces"})
        @ParameterizedTest(name = "slug = \"{0}\"")
        @DisplayName("then status code 400 should get returned")
        void shouldReturn400(String slug) {
            webTestClient.post().uri("/categories")
                         .contentType(APPLICATION_JSON)
                         .bodyValue(RandomDataProvider.randomPostCategoryRequestWithSlug(slug))
                         .exchange()
                         .expectStatus().isBadRequest();
        }

    }

    @Nested
    @DisplayName("Given a root category")
    class RootCategoryGiven {

        private PostCategoryResponse postCategoryResponse;

        @BeforeEach
        void setUp() {
            postCategoryResponse = categoryWebTestClient.postCategory(RandomDataProvider.randomPostCategoryRequest())
                                                        .getResponseBody();
        }

        @Nested
        @DisplayName("When POST /categories/{id} with invalid name")
        class PostChildCategoryWithInvalidName {

            @NullSource
            @EmptySource
            @ValueSource(strings = {" "})
            @ParameterizedTest(name = "name = \"{0}\"")
            @DisplayName("then status code 400 should get returned")
            void shouldReturn400(String name) {
                webTestClient.post().uri("/categories/{id}", postCategoryResponse.id())
                             .contentType(APPLICATION_JSON)
                             .bodyValue(RandomDataProvider.randomPostCategoryRequestWithName(name))
                             .exchange()
                             .expectStatus().isBadRequest();
            }

        }

        @Nested
        @DisplayName("When POST /categories/{id} with invalid slug")
        class PostChildCategoryWithInvalidSlug {

            @NullSource
            @EmptySource
            @ValueSource(strings = {" ", "slug containing whitespaces"})
            @ParameterizedTest(name = "slug = \"{0}\"")
            @DisplayName("then status code 400 should get returned")
            void shouldReturn400(String slug) {
                webTestClient.post().uri("/categories/{id}", postCategoryResponse.id())
                             .contentType(APPLICATION_JSON)
                             .bodyValue(RandomDataProvider.randomPostCategoryRequestWithSlug(slug))
                             .exchange()
                             .expectStatus().isBadRequest();
            }

        }

        @Nested
        @DisplayName("When PATCH /categories/{id} with invalid body")
        class PatchCategoryWithInvalidBody {

            @ValueSource(strings = {"{}", "{ \"visible\": null }"})
            @ParameterizedTest(name = "body = \"{0}\"")
            @DisplayName("then status code 400 should get returned")
            void shouldReturn400(String body) {
                webTestClient.patch().uri("/categories/{id}", postCategoryResponse.id())
                             .contentType(APPLICATION_JSON)
                             .bodyValue(body)
                             .exchange()
                             .expectStatus().isBadRequest();
            }

        }

    }

}
