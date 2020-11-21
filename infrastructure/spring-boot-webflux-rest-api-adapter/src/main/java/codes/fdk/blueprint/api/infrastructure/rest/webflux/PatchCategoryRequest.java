package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

record PatchCategoryRequest(@JsonProperty("isVisible") @NotNull Boolean isVisible) {}
