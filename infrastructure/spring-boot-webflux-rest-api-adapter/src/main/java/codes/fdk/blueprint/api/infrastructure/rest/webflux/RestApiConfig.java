package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.R2dbcPersistenceModule;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;
import static org.springframework.hateoas.support.WebStack.WEBFLUX;

@ComponentScan
@Configuration
@EnableAutoConfiguration
@Import(R2dbcPersistenceModule.class)
@EnableHypermediaSupport(type = HAL, stacks = WEBFLUX)
public class RestApiConfig {}
