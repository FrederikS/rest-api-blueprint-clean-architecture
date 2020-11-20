package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.service.CategoryService;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.GetCategoryResponse;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.ModelMapper;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.PatchCategoryRequest;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.PostCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.hateoas.IanaLinkRelations.SELF_VALUE;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static org.springframework.http.CacheControl.maxAge;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

//TODO HAL-Forms?

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    @Autowired
    public CategoryResource(CategoryService categoryService,
                            ModelMapper modelMapper) {
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getRootCategories() {
        return categoryService.all()
                              .as(this::toCollectionModel)
                              .zipWith(categoriesLink(SELF_VALUE), CollectionModel::add);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getCategory(@PathVariable CategoryId id, ServerWebExchange exchange) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .map(modelMapper::toResponse)
                              .flatMap(CategoryResource::toEntityModel)
                              .map(e -> {
                                  final String eTag = md5DigestAsHex(Integer.toString(e.hashCode()).getBytes());

                                  return exchange.checkNotModified(eTag)
                                          ? ResponseEntity.status(NOT_MODIFIED).eTag(eTag).build()
                                          : ResponseEntity.ok().cacheControl(maxAge(30, DAYS)).eTag(eTag).body(e);
                              });
    }

    @GetMapping("/{id}/children")
    public Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getChildCategories(@PathVariable CategoryId id) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .then(categoryService.children(id).as(this::toCollectionModel))
                              .zipWith(categoryChildrenLink(id, SELF_VALUE), CollectionModel::add);
    }

    @PostMapping
    public Mono<ResponseEntity<?>> postCategory(@Valid @RequestBody Mono<PostCategoryRequest> request) {
        return request.map(modelMapper::toCreateCommand)
                      .flatMap(this::saveAndReturnCreatedWithLocation);
    }

    @PostMapping("/{id}")
    public Mono<ResponseEntity<?>> postChildCategory(@PathVariable CategoryId id,
                                                     @Valid @RequestBody Mono<PostCategoryRequest> body) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .then(body.map(b -> modelMapper.toCreateCommand(id, b)))
                              .flatMap(this::saveAndReturnCreatedWithLocation);
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<?>> updateCategory(@PathVariable CategoryId id,
                                                  @Valid @RequestBody Mono<PatchCategoryRequest> body) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .then(body.map(b -> modelMapper.toUpdateCommand(id, b)))
                              .flatMap(categoryService::update)
                              .map(Category::id)
                              .flatMap(CategoryResource::categorySelfLink)
                              .map(location -> ResponseEntity.noContent()
                                                             .header(CONTENT_LOCATION, location.getHref())
                                                             .build());
    }

    private Mono<CollectionModel<EntityModel<GetCategoryResponse>>> toCollectionModel(Flux<Category> categories) {
        return categories.map(modelMapper::toResponse)
                         .flatMap(CategoryResource::toEntityModel)
                         .collectList()
                         .map(CollectionModel::of);
    }

    private static Mono<EntityModel<GetCategoryResponse>> toEntityModel(GetCategoryResponse response) {
        return Flux.merge(
                categoriesLink("categories"),
                categorySelfLink(response.id()),
                parentCategoryLink(response.parentId()),
                categoryChildrenLink(response.id(), "children")
        ).collect(() -> EntityModel.of(response), EntityModel::add);
    }

    private Mono<ResponseEntity<?>> saveAndReturnCreatedWithLocation(CreateCategoryCommand command) {
        return categoryService.create(command)
                              .map(Category::id)
                              .flatMap(CategoryResource::categorySelfLink)
                              .map(location -> ResponseEntity.created(location.toUri()).build());
    }

    private static Mono<Link> categoriesLink(String rel) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryResource.class).getRootCategories())
                                 .withRel(rel)
                                 .toMono();
    }

    private static Mono<Link> categorySelfLink(CategoryId id) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryResource.class).getCategory(id, null))
                                 .withSelfRel()
                                 .andAffordance(methodOn(CategoryResource.class).postChildCategory(id, null))
                                 .andAffordance(methodOn(CategoryResource.class).updateCategory(id, null))
                                 .toMono();
    }

    private static Mono<Link> categoryChildrenLink(CategoryId id, String rel) {
        return WebFluxLinkBuilder.linkTo(methodOn(CategoryResource.class).getChildCategories(id))
                                 .withRel(rel)
                                 .toMono();
    }

    private static Mono<Link> parentCategoryLink(@Nullable CategoryId parentId) {
        return Optional.ofNullable(parentId)
                       .map(pid -> WebFluxLinkBuilder.linkTo(methodOn(CategoryResource.class).getCategory(pid, null))
                                                     .withRel("parent")
                                                     .toMono())
                       .orElse(Mono.empty());
    }

}
