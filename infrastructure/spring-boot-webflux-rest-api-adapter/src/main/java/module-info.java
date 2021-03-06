module codes.fdk.blueprint.api.infrastructure.spring.rest.webflux {
    requires transitive codes.fdk.blueprint.api.domain;

    requires spring.web;
    requires spring.webflux;
    requires spring.hateoas;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.beans;
    requires spring.core;
    requires spring.context;
    requires reactor.core;
    requires org.mapstruct;
    requires com.fasterxml.jackson.annotation;
    requires org.reactivestreams;
    requires java.validation;
    requires org.hibernate.validator;
    requires io.swagger.v3.oas.annotations;

    exports codes.fdk.blueprint.api.infrastructure.spring.rest.webflux;
}