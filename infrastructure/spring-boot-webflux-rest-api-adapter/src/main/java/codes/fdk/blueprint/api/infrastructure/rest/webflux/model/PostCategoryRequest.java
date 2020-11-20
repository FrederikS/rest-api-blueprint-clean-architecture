package codes.fdk.blueprint.api.infrastructure.rest.webflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public record PostCategoryRequest(@JsonProperty("name") @NotBlank String name,
                                  @JsonProperty("slug") @Pattern(regexp = "[^\s]+") @NotBlank String slug,
                                  @JsonProperty("isVisible") boolean isVisible) {}
