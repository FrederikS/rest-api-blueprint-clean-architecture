package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec
import io.vertx.core.eventbus.impl.codecs.StringMessageCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient

class PostgresPersistenceVerticle : CoroutineVerticle() {

    private lateinit var pgClient: SqlClient
    private lateinit var categoryEntityRepositoryAdapter: CategoryEntityRepositoryAdapter
    private lateinit var categoryRepositoryEBProxyConsumer: MessageConsumer<Any>

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)

        vertx.eventBus()
            .registerDefaultCodec(Category::class.java, CategoryCodec(JsonObjectMessageCodec()))
            .registerDefaultCodec(CategoryId::class.java, CategoryIdCodec(StringMessageCodec()))

        val connectOptions = with(context.config()) {
            PgConnectOptions().apply {
                host = this@with.getString("PGHOST")
                port = this@with.getInteger("PGPORT")
                user = this@with.getString("PGUSERNAME")
                password = this@with.getString("PGPASSWORD")
                database = this@with.getString("PGDATABASE")
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
            .localConsumer<Any>(CategoryRepositoryEBProxy.ADDRESS)
            .handler(CategoryRepositoryEBProxyHandler(vertx, categoryEntityRepositoryAdapter))
    }

    override suspend fun stop() {
        super.stop()

        categoryRepositoryEBProxyConsumer.unregister().await()
    }

}
