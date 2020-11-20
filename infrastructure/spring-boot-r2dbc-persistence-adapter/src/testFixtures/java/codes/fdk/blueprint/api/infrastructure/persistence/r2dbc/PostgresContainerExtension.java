package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainerExtension implements BeforeAllCallback, AfterEachCallback {

    private static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:13-alpine");
        postgreSQLContainer.start();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        final String r2dbcUrl = String.format(
                "r2dbc:postgresql://%s:%d/%s",
                postgreSQLContainer.getHost(),
                postgreSQLContainer.getFirstMappedPort(),
                postgreSQLContainer.getDatabaseName()
        );

        System.setProperty("spring.r2dbc.url", r2dbcUrl);
        System.setProperty("spring.r2dbc.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.r2dbc.password", postgreSQLContainer.getPassword());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        postgreSQLContainer.execInContainer(
                "psql",
                "-U",
                postgreSQLContainer.getUsername(),
                "-d",
                postgreSQLContainer.getDatabaseName(),
                "-c",
                "TRUNCATE TABLE categories;"
        );
    }

}
