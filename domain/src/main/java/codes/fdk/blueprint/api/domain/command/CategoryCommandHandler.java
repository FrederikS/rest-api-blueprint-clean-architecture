package codes.fdk.blueprint.api.domain.command;

import codes.fdk.blueprint.api.domain.model.Category;
import reactor.core.publisher.Mono;

public interface CategoryCommandHandler {
    Mono<Category> create(CreateCategoryCommand command);
    Mono<Category> update(UpdateCategoryCommand command);
}
