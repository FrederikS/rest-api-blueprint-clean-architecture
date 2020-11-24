package codes.fdk.blueprint.api.domain.service;

import codes.fdk.blueprint.api.domain.command.CategoryCommandHandler;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService extends CategoryCommandHandler {
    Mono<Category> byId(CategoryId id);
    Flux<Category> all();
    Flux<Category> children(CategoryId parentId);

    static CategoryService create(CategoryRepository repository) {
        return new DefaultCategoryService(repository);
    }
}
