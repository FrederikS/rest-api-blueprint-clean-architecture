module codes.fdk.blueprint.api.application.vertx {
    requires codes.fdk.blueprint.api.infrastructure.vertx.eventbus;
    requires codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres;
    requires codes.fdk.blueprint.api.infrastructure.vertx.web.openapi;

    requires io.vertx.core;
    requires io.vertx.config;
}