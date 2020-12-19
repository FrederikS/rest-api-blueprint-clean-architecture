package codes.fdk.blueprint.api.infrastructure.json

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand
import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

//TODO checkout https://vertx.io/docs/vertx-json-schema/java/
object JsonMapper {

    fun fromCategoryId(id: CategoryId): JsonObject {
        return JsonObject(mapOf(
            "value" to id.value()
        ))
    }

    fun toCategoryId(json: JsonObject): CategoryId {
        return with(json) {
            getString("value").let { CategoryId.of(it) }
        }
    }

    fun fromCategory(category: Category): JsonObject {
        return with(category) {
            JsonObject(
                mapOf(
                    "id" to id()?.value(),
                    "name" to name(),
                    "slug" to slug(),
                    "parentId" to parentId()?.value(),
                    "visible" to visible(),
                )
            )
        }
    }

    fun toCategory(json: JsonObject): Category {
        return with(json) {
            Category(
                getString("id")?.let { CategoryId.of(it) },
                getString("name"),
                getString("slug"),
                getString("parentId")?.let { CategoryId.of(it) },
                getBoolean("visible")
            )
        }
    }


    fun fromCreateCategoryCommand(command: CreateCategoryCommand): JsonObject {
        return with(command) {
            JsonObject(
                mapOf(
                    "name" to name(),
                    "slug" to slug(),
                    "visible" to visible()
                )
            )
        }
    }

    fun toCreateCategoryCommand(json: JsonObject): CreateCategoryCommand {
        return with(json) {
            CreateCategoryCommand(
                getString("name"),
                getString("slug"),
                getString("parentId")?.let { CategoryId.of(it) },
                getBoolean("visible"),
            )
        }
    }

    fun fromUpdateCategoryCommand(command: UpdateCategoryCommand): JsonObject {
        return with(command) {
            JsonObject(
                mapOf(
                    "id" to id().value(),
                    "visible" to visible()
                )
            )
        }
    }

    fun toUpdateCategoryCommand(json: JsonObject): UpdateCategoryCommand {
        return with(json) {
            UpdateCategoryCommand(
                getString("id").let { CategoryId.of(it) },
                getBoolean("visible"),
            )
        }
    }

}