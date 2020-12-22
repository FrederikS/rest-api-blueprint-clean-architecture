package codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class CategoryEntityRepositoryAdapter implements CategoryRepository {

    private final CategoryEntityRepository categoryEntityRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Autowired
    public CategoryEntityRepositoryAdapter(CategoryEntityRepository categoryEntityRepository,
                                           CategoryEntityMapper categoryEntityMapper) {
        this.categoryEntityRepository = categoryEntityRepository;
        this.categoryEntityMapper = categoryEntityMapper;
    }

    @Override
    public Mono<Category> save(Category category) {
        Assert.notNull(category, "category must not be null!");
        return categoryEntityRepository.save(categoryEntityMapper.fromCategory(category))
                                       .map(this::toCategory);
    }

    @Override
    public Flux<Category> findAll() {
        return categoryEntityRepository.findAll()
                                       .map(this::toCategory);
    }

    @Override
    public Mono<Category> findById(CategoryId id) {
        return categoryEntityRepository.findById(id)
                                       .map(this::toCategory);
    }

    @Override
    public Flux<Category> findByParentId(CategoryId parentId) {
        return categoryEntityRepository.findByParentId(parentId)
                                       .map(this::toCategory);
    }

    private Category toCategory(CategoryEntity entity) {
        return categoryEntityMapper.toCategory(entity);
    }

}
