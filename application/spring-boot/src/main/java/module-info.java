module codes.fdk.blueprint.api.application.spring {
    requires codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc;
    requires codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}