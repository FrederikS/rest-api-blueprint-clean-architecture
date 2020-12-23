package codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
class CategoryWebTestClient {

    private final WebTestClient webTestClient;

    @Autowired
    protected CategoryWebTestClient(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    EntityExchangeResult<PostCategoryResponse> postCategory(PostCategoryRequest request) {
        return postCategory(request, URI.create("/categories"));
    }

    EntityExchangeResult<PostCategoryResponse> postChildCategory(PostCategoryRequest request,
                                                                 CategoryId parentId) {
        return postCategory(request, UriComponentsBuilder.fromPath("/categories/{id}").build(parentId));
    }

    private EntityExchangeResult<PostCategoryResponse> postCategory(PostCategoryRequest request,
                                                                    URI uri) {
        var postCategoryResponse = webTestClient.post()
                                                .uri(uri)
                                                .contentType(APPLICATION_JSON)
                                                .bodyValue(request)
                                                .exchange()
                                                .expectBody()
                                                .returnResult();

        //noinspection ConstantConditions
        return webTestClient.get().uri(postCategoryResponse.getResponseHeaders().getLocation())
                            .accept(HAL_JSON)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(PostCategoryResponse.class)
                            .returnResult();
    }

    static record PostCategoryResponse(@JsonProperty("id") CategoryId id) {}

}
