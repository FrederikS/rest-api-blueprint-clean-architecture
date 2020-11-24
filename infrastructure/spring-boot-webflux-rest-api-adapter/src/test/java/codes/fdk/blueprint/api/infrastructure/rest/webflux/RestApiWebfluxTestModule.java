package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.service.CategoryService;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SpringBootApplication
public class RestApiWebfluxTestModule {

    @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return CategoryService.create(categoryRepository);
    }

    @Component
    static class InMemoryCategoryRepository implements CategoryRepository {

        static final Map<CategoryId, Category> STORE = new HashMap<>();

        @Override
        public Mono<Category> save(Category category) {
            return Mono.fromCallable(() -> {
                Objects.requireNonNull(category);
                final Category categoryWithId = assignIdWhenMissing(category);
                return STORE.compute(categoryWithId.id(), (k, v) -> categoryWithId);
            });
        }

        @Override
        public Flux<Category> findAll() {
            return Flux.fromIterable(STORE.values());
        }

        @Override
        public Mono<Category> findById(CategoryId id) {
            return Mono.defer(() -> {
                Objects.requireNonNull(id);
                return Mono.justOrEmpty(STORE.get(id));
            });
        }

        @Override
        public Flux<Category> findByParentId(CategoryId parentId) {
            return Flux.defer(() -> {
                Objects.requireNonNull(parentId);
                return Flux.fromIterable(STORE.values())
                           .filter(c -> parentId.equals(c.parentId()));
            });
        }

        private static Category assignIdWhenMissing(Category category) {
            if (category.id() == null) {
                return new Category(
                        CategoryId.of(UUID.randomUUID().toString()),
                        category.name(),
                        category.slug(),
                        category.parentId(),
                        category.isVisible()
                );
            }

            return category;
        }
    }

}
