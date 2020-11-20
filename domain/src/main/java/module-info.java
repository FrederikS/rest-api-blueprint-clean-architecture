module codes.fdk.blueprint.api.domain {
    requires reactor.core;
    requires org.mapstruct;
    requires com.fasterxml.jackson.annotation;
    requires static jsr305;

    exports codes.fdk.blueprint.api.domain.model;
    exports codes.fdk.blueprint.api.domain.spi to codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;
    exports codes.fdk.blueprint.api.domain.service to codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;
}