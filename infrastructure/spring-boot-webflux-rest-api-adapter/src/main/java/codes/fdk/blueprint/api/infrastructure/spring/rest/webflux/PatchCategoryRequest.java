package codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

record PatchCategoryRequest(@JsonProperty("visible") @NotNull Boolean visible) {}
