module codes.fdk.blueprint.api.domain {
    requires reactor.core;
    requires org.mapstruct;
    requires com.fasterxml.jackson.annotation;
    requires static jsr305;

    exports codes.fdk.blueprint.api.domain.model;
    exports codes.fdk.blueprint.api.domain.command to codes.fdk.blueprint.api.infrastructure.spring.rest.webflux, codes.fdk.blueprint.api.infrastructure.vertx.eventbus, codes.fdk.blueprint.api.infrastructure.vertx.web.openapi;
    exports codes.fdk.blueprint.api.domain.spi to codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc, codes.fdk.blueprint.api.application.spring, codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres, codes.fdk.blueprint.api.infrastructure.vertx.eventbus;
    exports codes.fdk.blueprint.api.domain.service to codes.fdk.blueprint.api.infrastructure.spring.rest.webflux, codes.fdk.blueprint.api.application.spring, codes.fdk.blueprint.api.infrastructure.vertx.eventbus, codes.fdk.blueprint.api.infrastructure.vertx.web.openapi, codes.fdk.blueprint.api.application.vertx;
}