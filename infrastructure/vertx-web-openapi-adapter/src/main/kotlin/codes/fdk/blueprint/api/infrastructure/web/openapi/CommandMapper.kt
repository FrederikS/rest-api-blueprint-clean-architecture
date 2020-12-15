package codes.fdk.blueprint.api.infrastructure.web.openapi

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand

object CommandMapper {

    fun toCommand(request: PostCategoryRequest): CreateCategoryCommand {
        return CreateCategoryCommand.createRootCategoryCommand(
            request.name,
            request.slug,
            request.isVisible
        )
    }

}