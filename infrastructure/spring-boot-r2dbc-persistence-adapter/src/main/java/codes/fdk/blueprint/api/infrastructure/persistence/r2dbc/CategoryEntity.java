package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

@Table("categories")
record CategoryEntity(@Id @Nullable CategoryId id,
                      String name,
                      String slug,
                      @Nullable CategoryId parentId,
                      boolean visible) {}
