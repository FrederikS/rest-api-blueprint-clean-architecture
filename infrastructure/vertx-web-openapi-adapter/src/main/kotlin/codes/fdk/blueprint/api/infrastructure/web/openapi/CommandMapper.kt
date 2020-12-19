package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.CategoryId

object CommandMapper {

    fun toCommand(request: PostCategoryRequest): CreateCategoryCommand {
        return CreateCategoryCommand.createRootCategoryCommand(
            request.name,
            request.slug,
            request.visible
        )
    }

    fun toCommand(id: CategoryId, request: PatchCategoryRequest): UpdateCategoryCommand {
        return UpdateCategoryCommand(id, request.visible)
    }

}