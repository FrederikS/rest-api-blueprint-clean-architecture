package codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

record PostCategoryRequest(@JsonProperty("name") @NotBlank String name,
                           @JsonProperty("slug") @Pattern(regexp = "[^\s]+") @NotBlank String slug,
                           @JsonProperty("visible") boolean visible) {}
