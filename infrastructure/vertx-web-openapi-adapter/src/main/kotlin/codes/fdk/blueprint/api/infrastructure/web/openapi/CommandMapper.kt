package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.CategoryId

internal object CommandMapper {

    fun toCreateCommand(request: PostCategoryRequest): CreateCategoryCommand {
        return CreateCategoryCommand.createRootCategoryCommand(
            request.name,
            request.slug,
            request.visible
        )
    }

    fun toCreateCommand(parentId: CategoryId, request: PostCategoryRequest): CreateCategoryCommand {
        return CreateCategoryCommand.createChildCategoryCommand(
            request.name,
            request.slug,
            parentId,
            request.visible
        )
    }

    fun toUpdateCommand(id: CategoryId, request: PatchCategoryRequest): UpdateCategoryCommand {
        return UpdateCategoryCommand(id, request.visible)
    }

}