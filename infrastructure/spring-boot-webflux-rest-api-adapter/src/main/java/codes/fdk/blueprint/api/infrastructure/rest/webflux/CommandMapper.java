package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand;
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.mapstruct.Mapper;

import static codes.fdk.blueprint.api.domain.command.CreateCategoryCommand.createChildCategoryCommand;
import static codes.fdk.blueprint.api.domain.command.CreateCategoryCommand.createRootCategoryCommand;

//TODO figure out how to let mapstruct do the work
@Mapper
interface CommandMapper {

    default CreateCategoryCommand toCreateCommand(PostCategoryRequest request) {
        return createRootCategoryCommand(
                request.name(),
                request.slug(),
                request.isVisible()
        );
    }

    default CreateCategoryCommand toCreateCommand(CategoryId parentId, PostCategoryRequest request) {
        return createChildCategoryCommand(
                request.name(),
                request.slug(),
                parentId,
                request.isVisible()
        );
    }

    default UpdateCategoryCommand toUpdateCommand(CategoryId id, PatchCategoryRequest request) {
        return new UpdateCategoryCommand(id, request.isVisible());
    }

    default GetCategoryResponse toResponse(Category category) {
        return new GetCategoryResponse(
                category.id(),
                category.name(),
                category.slug(),
                category.parentId(),
                category.isVisible()
        );
    }

}
