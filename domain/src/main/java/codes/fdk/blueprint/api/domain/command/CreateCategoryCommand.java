package codes.fdk.blueprint.api.domain.command;

import codes.fdk.blueprint.api.domain.model.CategoryId;

import javax.annotation.Nullable;

public record CreateCategoryCommand(String name, String slug, @Nullable CategoryId parentId, boolean isVisible) {

    public static CreateCategoryCommand createRootCategoryCommand(String name,
                                                                  String slug,
                                                                  boolean isVisible) {
        return new CreateCategoryCommand(name, slug, null, isVisible);
    }

    public static CreateCategoryCommand createChildCategoryCommand(String name,
                                                                   String slug,
                                                                   CategoryId parentId,
                                                                   boolean isVisible) {
        return new CreateCategoryCommand(name, slug, parentId, isVisible);
    }

}
