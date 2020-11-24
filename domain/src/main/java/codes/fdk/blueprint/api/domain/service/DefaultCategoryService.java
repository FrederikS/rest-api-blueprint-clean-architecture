package codes.fdk.blueprint.api.domain.service;

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand;
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class DefaultCategoryService implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper = CategoryMapper.INSTANCE;

    DefaultCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Mono<Category> create(CreateCategoryCommand command) {
        return categoryRepository.save(categoryMapper.toCategory(command));
    }

    @Override
    public Mono<Category> byId(CategoryId id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Flux<Category> all() {
        return categoryRepository.findAll();
    }

    @Override
    public Flux<Category> children(CategoryId parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public Mono<Category> update(UpdateCategoryCommand command) {
        return categoryRepository.findById(command.id())
                                 .map(e -> categoryMapper.updateCategory(e, command))
                                 .flatMap(categoryRepository::save);
    }

}
