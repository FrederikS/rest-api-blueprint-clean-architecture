package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.GetCategoryResponse;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.PostCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
class CategoryWebTestClient {

    private final WebTestClient webTestClient;

    @Autowired
    protected CategoryWebTestClient(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    EntityExchangeResult<EntityModel<GetCategoryResponse>> postCategory(PostCategoryRequest request) {
        return postCategory(request, URI.create("/categories"));
    }

    EntityExchangeResult<EntityModel<GetCategoryResponse>> postChildCategory(PostCategoryRequest request,
                                                                             CategoryId parentId) {
        return postCategory(request, UriComponentsBuilder.fromPath("/categories/{id}").build(parentId));
    }

    private EntityExchangeResult<EntityModel<GetCategoryResponse>> postCategory(PostCategoryRequest request,
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
                            .accept(APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(new ParameterizedTypeReference<EntityModel<GetCategoryResponse>>() {})
                            .value(body -> assertThat(body).isNotNull())
                            .returnResult();
    }

}
