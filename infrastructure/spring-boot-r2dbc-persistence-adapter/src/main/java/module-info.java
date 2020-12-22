module codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc {
    requires transitive codes.fdk.blueprint.api.domain;

    requires spring.core;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.data.commons;
    requires spring.data.relational;
    requires spring.data.r2dbc;
    requires reactor.core;
    requires r2dbc.spi;
    requires org.mapstruct;

    exports codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc;
}