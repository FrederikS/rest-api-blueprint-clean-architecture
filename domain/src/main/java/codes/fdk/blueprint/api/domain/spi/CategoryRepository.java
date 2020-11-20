package codes.fdk.blueprint.api.domain.spi;

import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository {

    Mono<Category> save(Category category);
    Flux<Category> findAll();
    Mono<Category> findById(CategoryId id);
    Flux<Category> findByParentId(CategoryId parentId);

}
