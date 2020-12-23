package codes.fdk.blueprint.api.infrastructure.vertx.web.openapi

import com.github.javafaker.Faker

internal object RandomDataProvider {

    private val FAKER = Faker()

    fun randomPostCategoryRequest(): PostCategoryRequest {
        return PostCategoryRequest(
            name = FAKER.commerce().department(),
            slug = FAKER.internet().slug(),
            visible = FAKER.bool().bool()
        )
    }

}