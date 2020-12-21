package codes.fdk.blueprint.api.infrastructure.persistence.postgres

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

        val connectOptions = with(context.config()) {
            PgConnectOptions().apply {
                host = this@with.getString("PGHOST", "localhost")
                port = this@with.getInteger("PGPORT", 5432)
                user = this@with.getString("PGUSERNAME", "postgres")
                password = this@with.getString("PGPASSWORD", "pw")
                database = this@with.getString("PGDATABASE", "postgres")
            }
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
            .handler(CategoryRepositoryEBProxyHandler(vertx, categoryEntityRepositoryAdapter))
    }

    override suspend fun stop() {
        super.stop()

        categoryRepositoryEBProxyConsumer.unregister().await()
    }

}
