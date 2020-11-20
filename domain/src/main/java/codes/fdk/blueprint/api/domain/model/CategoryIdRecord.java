package codes.fdk.blueprint.api.domain.model;

import javax.annotation.Nullable;

record CategoryIdRecord(@Nullable String value) implements CategoryId {

    @Override
    public String toString() {
        return value;
    }

}
