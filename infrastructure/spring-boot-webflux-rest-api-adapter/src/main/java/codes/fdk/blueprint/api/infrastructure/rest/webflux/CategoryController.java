package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(value = "/categories", produces = HAL_JSON_VALUE)
interface CategoryController {

    @GetMapping
    Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getRootCategories();

    @GetMapping("/{id}")
    Mono<ResponseEntity<EntityModel<GetCategoryResponse>>> getCategory(@PathVariable CategoryId id,
                                                                       ServerWebExchange exchange);

    @GetMapping("/{id}/children")
    Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getChildCategories(@PathVariable CategoryId id);

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> postCategory(@Valid @RequestBody Mono<PostCategoryRequest> request);

    @PostMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> postChildCategory(@PathVariable CategoryId id,
                                                 @Valid @RequestBody Mono<PostCategoryRequest> body);

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> updateCategory(@PathVariable CategoryId id,
                                              @Valid @RequestBody Mono<PatchCategoryRequest> body);

}
