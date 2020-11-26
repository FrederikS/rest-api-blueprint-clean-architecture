package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

final class HalModelAssembler {

    private HalModelAssembler() {
    }

    static Mono<EntityModel<GetCategoryResponse>> toHalModel(GetCategoryResponse response) {
        return Flux.merge(
                categoriesLink("categories"),
                categorySelfLink(response.id()),
                parentCategoryLink(response.parentId()),
                categoryChildrenLink(response.id(), "children")
        ).collect(() -> EntityModel.of(response), EntityModel::add);
    }

    static Mono<CollectionModel<EntityModel<GetCategoryResponse>>> toHalCollectionModel(Flux<GetCategoryResponse> categories) {
        return categories.flatMap(HalModelAssembler::toHalModel)
                         .collectList()
                         .map(CollectionModel::of)
                         .defaultIfEmpty(CollectionModel.empty());
    }

    static Mono<Link> categoriesLink(String rel) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryController.class).getRootCategories())
                                 .withRel(rel)
                                 .toMono();
    }

    static Mono<Link> categorySelfLink(CategoryId id) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryController.class).getCategory(id, null))
                                 .withSelfRel()
                                 .andAffordance(methodOn(CategoryController.class).postChildCategory(id, null))
                                 .andAffordance(methodOn(CategoryController.class).updateCategory(id, null))
                                 .toMono();
    }

    static Mono<Link> categoryChildrenLink(CategoryId id, String rel) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryController.class).getChildCategories(id))
                                 .withRel(rel)
                                 .toMono();
    }

    private static Mono<Link> parentCategoryLink(@Nullable CategoryId parentId) {
        return Optional.ofNullable(parentId)
                       .map(pid -> WebFluxLinkBuilder.linkTo(methodOn(CategoryController.class).getCategory(pid, null))
                                                     .withRel("parent")
                                                     .toMono())
                       .orElse(Mono.empty());
    }

}
