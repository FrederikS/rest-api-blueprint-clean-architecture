package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.PostgresContainerExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PostgresContainerExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public @interface IntegrationTest {}
