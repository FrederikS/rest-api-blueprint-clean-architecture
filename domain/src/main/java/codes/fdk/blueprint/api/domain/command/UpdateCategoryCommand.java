package codes.fdk.blueprint.api.domain.command;

import codes.fdk.blueprint.api.domain.model.CategoryId;

public record UpdateCategoryCommand(CategoryId id, boolean visible) {}
