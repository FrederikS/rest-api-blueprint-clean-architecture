package codes.fdk.blueprint.api.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public interface CategoryId {

    @JsonValue
    String value();

    @JsonCreator
    static CategoryId of(String value) {
        Objects.requireNonNull(value);
        return new CategoryIdRecord(value);
    }

}
