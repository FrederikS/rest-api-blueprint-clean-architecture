package codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainerExtension : BeforeAllCallback, AfterEachCallback {

    companion object {
        val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:13-alpine").apply {
            start()
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        System.setProperty("PGHOST", postgreSQLContainer.containerIpAddress)
        System.setProperty("PGPORT", postgreSQLContainer.firstMappedPort.toString())
        System.setProperty("PGUSERNAME", postgreSQLContainer.username)
        System.setProperty("PGPASSWORD", postgreSQLContainer.password)
        System.setProperty("PGDATABASE", postgreSQLContainer.databaseName)
    }

    override fun afterEach(context: ExtensionContext?) {
        postgreSQLContainer.execInContainer(
            "psql",
            "-U",
            postgreSQLContainer.username,
            "-d",
            postgreSQLContainer.databaseName,
            "-c",
            "TRUNCATE TABLE categories;"
        )
    }


}