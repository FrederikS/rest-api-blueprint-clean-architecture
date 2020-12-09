package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec
import io.vertx.core.eventbus.impl.codecs.StringMessageCodec
import io.vertx.core.json.JsonObject

internal class CategoryCodec(private val delegate: JsonObjectMessageCodec) : MessageCodec<Category, Category> {

    override fun encodeToWire(buffer: Buffer, s: Category) {
        delegate.encodeToWire(buffer, JsonObject.mapFrom(s))
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): Category {
        return delegate.decodeFromWire(pos, buffer).toCategory()
    }

    override fun transform(s: Category): Category {
        return s
    }

    override fun name(): String {
        return "${javaClass.simpleName}Codec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    private fun JsonObject.toCategory(): Category {
        return Category(
            this.getString("id")?.let { CategoryId.of(it) },
            this.getString("name"),
            this.getString("slug"),
            this.getString("parentId")?.let { CategoryId.of(it) },
            this.getBoolean("visible")
        )
    }

}

// TODO make it work for interface type
internal class CategoryIdCodec(private val delegate: StringMessageCodec) : MessageCodec<CategoryId, CategoryId> {

    override fun encodeToWire(buffer: Buffer, s: CategoryId) {
        delegate.encodeToWire(buffer, s.value())
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): CategoryId {
        return CategoryId.of(delegate.decodeFromWire(pos, buffer))
    }

    override fun transform(s: CategoryId): CategoryId {
        return s
    }

    override fun name(): String {
        return "${javaClass.simpleName}Codec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

}
