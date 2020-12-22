module codes.fdk.blueprint.api.application.vertx {
    requires codes.fdk.blueprint.api.infrastructure.eventbus;
    requires codes.fdk.blueprint.api.infrastructure.persistence.postgres;
    requires codes.fdk.blueprint.api.infrastructure.web.openapi;

    requires io.vertx.core;
    requires io.vertx.config;
}