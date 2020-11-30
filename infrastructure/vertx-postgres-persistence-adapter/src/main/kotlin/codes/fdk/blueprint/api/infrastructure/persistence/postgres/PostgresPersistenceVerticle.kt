package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient

class PostgresPersistenceVerticle : CoroutineVerticle() {

    private lateinit var pgClient: SqlClient

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)

        pgClient = PgPool.pool(vertx, PoolOptions())
    }

    override suspend fun start() {
        super.start()

        val schema = this::class.java.getResource("/schema.sql").readText()
        pgClient.query(schema).execute().await()
    }

}