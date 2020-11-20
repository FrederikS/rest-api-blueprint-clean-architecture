package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;
import static org.springframework.hateoas.support.WebStack.WEBFLUX;

@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableHypermediaSupport(type = HAL, stacks = WEBFLUX)
public class RestApiConfig {}
