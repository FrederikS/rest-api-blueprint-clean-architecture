package codes.fdk.blueprint.api.infrastructure.rest.webflux.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public record PatchCategoryRequest(@JsonProperty("isVisible") @NotNull Boolean isVisible) {}
