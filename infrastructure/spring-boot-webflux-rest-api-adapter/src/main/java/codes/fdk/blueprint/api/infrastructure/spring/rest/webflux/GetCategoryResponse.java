package codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;

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
                           @JsonProperty("visible") boolean visible) {}
