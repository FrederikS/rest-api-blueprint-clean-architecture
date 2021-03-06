package codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres

import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxy
import codes.fdk.blueprint.api.infrastructure.vertx.eventbus.CategoryRepositoryEBProxyHandler
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient

class PostgresPersistenceVerticle : CoroutineVerticle() {

    private lateinit var pgClient: SqlClient
    private lateinit var categoryEntityRepositoryAdapter: CategoryEntityRepositoryAdapter
    private lateinit var categoryRepositoryEBProxyConsumer: MessageConsumer<JsonObject>

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)

        val connectOptions = PgConnectOptions().apply {
            host = config.getString("PGHOST")
            port = config.getInteger("PGPORT")
            user = config.getString("PGUSERNAME")
            password = config.getString("PGPASSWORD")
            database = config.getString("PGDATABASE")
        }

        pgClient = PgPool.pool(vertx, connectOptions, PoolOptions())
        categoryEntityRepositoryAdapter = CategoryEntityRepositoryAdapter(PostgresCategoryEntityRepository(pgClient))
    }

    override suspend fun start() {
        super.start()

        val schema = this::class.java.getResource("/schema.sql").readText()
        pgClient.query(schema).execute().await()

        categoryRepositoryEBProxyConsumer = vertx.eventBus()
            .localConsumer<JsonObject>(CategoryRepositoryEBProxy.ADDRESS)
            .handler(CategoryRepositoryEBProxyHandler(categoryEntityRepositoryAdapter))
    }

    override suspend fun stop() {
        super.stop()

        categoryRepositoryEBProxyConsumer.unregister().await()
    }

}
