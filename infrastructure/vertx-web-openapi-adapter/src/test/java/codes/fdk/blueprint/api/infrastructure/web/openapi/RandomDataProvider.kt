package codes.fdk.blueprint.api.infrastructure.web.openapi

import com.github.javafaker.Faker

object RandomDataProvider {

    private val FAKER = Faker()

    fun randomPostCategoryRequest(): PostCategoryRequest {
        return PostCategoryRequest(
            name = FAKER.commerce().department(),
            slug = FAKER.internet().slug(),
            isVisible = FAKER.bool().bool()
        )
    }

}