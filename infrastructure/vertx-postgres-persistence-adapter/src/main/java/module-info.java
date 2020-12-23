module codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres {
    requires transitive codes.fdk.blueprint.api.domain;
    requires codes.fdk.blueprint.api.infrastructure.vertx.eventbus;

    requires org.slf4j;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.reactor;
    requires io.vertx.core;
    requires io.vertx.client.sql.pg;
    requires io.vertx.client.sql;
    requires io.vertx.kotlin.coroutines;
    requires reactor.core;

    exports codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres;
}