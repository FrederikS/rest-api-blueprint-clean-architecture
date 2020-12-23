module codes.fdk.blueprint.api.infrastructure.vertx.eventbus {
    requires transitive codes.fdk.blueprint.api.domain;

    requires reactor.core;
    requires io.vertx.core;
    requires io.vertx.kotlin.coroutines;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.reactor;

    exports codes.fdk.blueprint.api.infrastructure.vertx.eventbus;
}