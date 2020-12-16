package codes.fdk.blueprint.api.domain.model;

import javax.annotation.Nullable;

public record Category(CategoryId id, String name, String slug, @Nullable CategoryId parentId, boolean visible) {}
