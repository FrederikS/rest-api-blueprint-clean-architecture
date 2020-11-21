package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
record GetCategoryResponse(@JsonProperty("id") CategoryId id,
                           @JsonProperty("name") String name,
                           @JsonProperty("slug") String slug,
                           @JsonProperty("parentId") @Nullable CategoryId parentId,
                           @JsonProperty("isVisible") boolean isVisible) {

    public GetCategoryResponse(CategoryId id, String name, String slug, boolean isVisible) {
        this(id, name, slug, null, isVisible);
    }

}