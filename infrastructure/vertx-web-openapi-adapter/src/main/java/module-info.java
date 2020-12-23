module codes.fdk.blueprint.api.infrastructure.vertx.web.openapi {
    requires codes.fdk.blueprint.api.domain;
    requires codes.fdk.blueprint.api.infrastructure.vertx.eventbus;

    requires org.slf4j;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.reactive;
    requires com.fasterxml.jackson.annotation;
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.vertx.web.openapi;
    requires io.vertx.kotlin.coroutines;
    requires reactor.core;

    exports codes.fdk.blueprint.api.infrastructure.vertx.web.openapi;
}