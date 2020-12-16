package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.CategoryId
import io.github.serpro69.kfaker.Faker
import java.util.UUID

internal object RandomDataProvider {

    private val FAKER = Faker()

    fun randomId(): CategoryId = CategoryId.of(UUID.randomUUID().toString())

    fun randomCategory(): CategoryEntity {
        val name = FAKER.commerce.department()

        return CategoryEntity(
            id = null,
            name = name,
            slug = "slug",
            parentId = null,
            visible = true
        )
    }

    fun randomCategoryWithId(): CategoryEntity {
        val randomCategory = randomCategory()

        return CategoryEntity(
            id = randomId(),
            name = randomCategory.name,
            slug = randomCategory.slug,
            parentId = randomCategory.parentId,
            visible = randomCategory.visible
        )
    }

    fun randomChildCategory(): CategoryEntity {
        val randomCategory = randomCategory()

        return CategoryEntity(
            id = randomCategory.id,
            name = randomCategory.name,
            slug = randomCategory.slug,
            parentId = CategoryId.of(UUID.randomUUID().toString()),
            visible = randomCategory.visible
        )
    }

}