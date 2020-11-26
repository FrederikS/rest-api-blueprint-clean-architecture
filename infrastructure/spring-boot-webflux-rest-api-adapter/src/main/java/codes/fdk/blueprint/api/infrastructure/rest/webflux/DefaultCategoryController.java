package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static codes.fdk.blueprint.api.infrastructure.rest.webflux.HalModelAssembler.categoriesLink;
import static codes.fdk.blueprint.api.infrastructure.rest.webflux.HalModelAssembler.categoryChildrenLink;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.hateoas.IanaLinkRelations.SELF_VALUE;
import static org.springframework.http.CacheControl.maxAge;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

//TODO HAL-Forms?
@RestController
class DefaultCategoryController implements CategoryController {

    private final CategoryService categoryService;
    private final CommandMapper commandMapper;

    @Autowired
    public DefaultCategoryController(CategoryService categoryService,
                                     CommandMapper commandMapper) {
        this.categoryService = categoryService;
        this.commandMapper = commandMapper;
    }

    @Override
    public Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getRootCategories() {
        return categoryService.all()
                              .map(commandMapper::toResponse)
                              .as(HalModelAssembler::toHalCollectionModel)
                              .zipWith(categoriesLink(SELF_VALUE), CollectionModel::add);
    }

    @Override
    public Mono<ResponseEntity<EntityModel<GetCategoryResponse>>> getCategory(CategoryId id,
                                                                              ServerWebExchange exchange) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .map(commandMapper::toResponse)
                              .flatMap(HalModelAssembler::toHalModel)
                              .map(e -> {
                                  final String eTag = md5DigestAsHex(Integer.toString(e.hashCode()).getBytes());

                                  return exchange.checkNotModified(eTag)
                                          ? ResponseEntity.status(NOT_MODIFIED).eTag(eTag).build()
                                          : ResponseEntity.ok().cacheControl(maxAge(30, DAYS)).eTag(eTag).body(e);
                              });
    }

    @Override
    public Mono<CollectionModel<EntityModel<GetCategoryResponse>>> getChildCategories(CategoryId id) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .thenMany(categoryService.children(id))
                              .map(commandMapper::toResponse)
                              .as(HalModelAssembler::toHalCollectionModel)
                              .zipWith(categoryChildrenLink(id, SELF_VALUE), CollectionModel::add);
    }

    @Override
    public Mono<ResponseEntity<Void>> postCategory(@Valid Mono<PostCategoryRequest> request) {
        return request.map(commandMapper::toCreateCommand)
                      .flatMap(this::saveAndReturnCreatedWithLocation);
    }

    @Override
    public Mono<ResponseEntity<Void>> postChildCategory(CategoryId id, @Valid Mono<PostCategoryRequest> body) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .then(body.map(b -> commandMapper.toCreateCommand(id, b)))
                              .flatMap(this::saveAndReturnCreatedWithLocation);
    }

    @Override
    public Mono<ResponseEntity<Void>> updateCategory(CategoryId id, @Valid Mono<PatchCategoryRequest> body) {
        return categoryService.byId(id)
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND)))
                              .then(body.map(b -> commandMapper.toUpdateCommand(id, b)))
                              .flatMap(categoryService::update)
                              .map(Category::id)
                              .flatMap(HalModelAssembler::categorySelfLink)
                              .map(location -> ResponseEntity.noContent()
                                                             .header(CONTENT_LOCATION, location.getHref())
                                                             .build());
    }

    private Mono<ResponseEntity<Void>> saveAndReturnCreatedWithLocation(CreateCategoryCommand command) {
        return categoryService.create(command)
                              .map(Category::id)
                              .flatMap(HalModelAssembler::categorySelfLink)
                              .map(location -> ResponseEntity.created(location.toUri()).build());
    }

}
