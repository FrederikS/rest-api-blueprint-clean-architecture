package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.RandomDataProvider.randomCategory
import codes.fdk.blueprint.api.domain.RandomDataProvider.randomCategoryWithId
import codes.fdk.blueprint.api.domain.RandomDataProvider.randomChildCategory
import codes.fdk.blueprint.api.domain.RandomDataProvider.randomId
import codes.fdk.blueprint.api.domain.model.Category
import io.vertx.config.ConfigRetriever
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.from
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.test.StepVerifier

@ExtendWith(VertxExtension::class, PostgresContainerExtension::class)
internal class CategoryRepositoryEBProxyTest {

    companion object {

        private lateinit var categoryRepositoryEBProxy: CategoryRepositoryEBProxy

        @JvmStatic
        @BeforeAll
        fun init(vertx: Vertx, context: VertxTestContext) {
            categoryRepositoryEBProxy = CategoryRepositoryEBProxy(vertx)

            ConfigRetriever.create(vertx)
                .config
                .compose { vertx.deployVerticle(PostgresPersistenceVerticle(), DeploymentOptions().setConfig(it)) }
                .onComplete(context.succeedingThenComplete())
                .onFailure { context.failNow(it) }
        }

    }

    @Nested
    @DisplayName("When findById for non-existent ID")
    internal inner class FindById {

        @Test
        @DisplayName("then null should get returned")
        internal fun shouldReturnEmpty() {
            StepVerifier.create(categoryRepositoryEBProxy.findById(randomId()))
                .verifyComplete()
        }

    }

    @Nested
    @DisplayName("When insert a category without id")
    internal inner class InsertCategoryWithoutId {

        @Test
        @DisplayName("then saved category should get returned")
        internal fun shouldReturnSavedCategory() {
            val categoryToSave = randomCategory()

            StepVerifier.create(categoryRepositoryEBProxy.save(categoryToSave))
                .assertNext { category ->
                    assertThat(category)
                        .returns(categoryToSave.name(), from { it.name() })
                        .returns(categoryToSave.slug(), from { it.slug() })
                        .returns(categoryToSave.parentId(), from { it.parentId() })
                        .returns(categoryToSave.visible(), from { it.visible() })
                }
                .verifyComplete()
        }

        @Test
        @DisplayName("then saved category should get an id assigned")
        internal fun shouldGetAnId() {
            val categoryToSave = randomCategory()
            assertThat(categoryToSave.id()).isNull()

            StepVerifier.create(categoryRepositoryEBProxy.save(categoryToSave))
                .assertNext { category ->
                    assertThat(category.id()).isNotNull
                        .extracting { it.value() }
                        .asString()
                        .isNotBlank()
                }
                .verifyComplete()
        }

    }

    @Nested
    @DisplayName("When save a category with non-existent id")
    internal inner class InsertWithNonExistentId {

        @Test
        @DisplayName("then save should return an error")
        internal fun shouldReturnAnError() {
            StepVerifier.create(categoryRepositoryEBProxy.save(randomCategoryWithId()))
                .verifyError()
        }

    }

    @Nested
    @DisplayName("When query all entities on empty repository")
    internal inner class FindAll {

        @Test
        @DisplayName("then nothing should get returned")
        internal fun shouldReturnEmptyPublisher() {
            StepVerifier.create(categoryRepositoryEBProxy.findAll())
                .verifyComplete()
        }

    }

    @Nested
    @DisplayName("When query by non-existent parentId")
    internal inner class FindByParent {

        @Test
        @DisplayName("then nothing should get returned")
        internal fun shouldReturnEmptyPublisher() {
            StepVerifier.create(categoryRepositoryEBProxy.findByParentId(randomId()))
                .verifyComplete()
        }

    }

    @Nested
    @DisplayName("Given a saved category")
    internal inner class CategoryGiven {

        private lateinit var savedCategory: Category

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            runBlocking {
                savedCategory = categoryRepositoryEBProxy.save(randomCategory()).awaitFirst()
                context.completeNow()
            }
        }

        @Nested
        @DisplayName("when save for category with id")
        internal inner class Update {

            @Test
            @DisplayName("then saved category should get updated")
            fun shouldReturnCategory() {
                val updatedCategory = Category(
                    savedCategory.id(),
                    savedCategory.name(),
                    savedCategory.slug(),
                    savedCategory.parentId(),
                    !savedCategory.visible()
                )

                StepVerifier.create(categoryRepositoryEBProxy.save(updatedCategory))
                    .assertNext {
                        assertThat(it).isNotNull
                            .returns(updatedCategory.id(), from(Category::id))
                            .returns(updatedCategory.name(), from(Category::name))
                            .returns(updatedCategory.slug(), from(Category::slug))
                            .returns(updatedCategory.parentId(), from(Category::parentId))
                            .returns(updatedCategory.visible(), from(Category::visible))
                    }.verifyComplete()
            }

        }

        @Nested
        @DisplayName("when findById for saved category")
        internal inner class FindById {

            @Test
            @DisplayName("then saved category should get returned")
            fun shouldReturnCategory() {
                StepVerifier.create(categoryRepositoryEBProxy.findById(savedCategory.id()))
                    .expectNext(savedCategory)
                    .verifyComplete()
            }

        }

        @Nested
        @DisplayName("when findAll")
        internal inner class FindAll {

            @Test
            @DisplayName("then saved category should get returned")
            fun shouldReturnSavedCategory() {
                StepVerifier.create(categoryRepositoryEBProxy.findAll())
                    .expectNext(savedCategory)
                    .verifyComplete()
            }

            @Test
            @DisplayName("then all saved categories should get returned")
            fun shouldReturnAllCategories() {
                runBlocking {
                    categoryRepositoryEBProxy.save(randomCategory()).awaitFirst()
                }

                StepVerifier.create(categoryRepositoryEBProxy.findAll())
                    .expectNextCount(2)
                    .verifyComplete()
            }

        }

    }

    @Nested
    @DisplayName("Given a saved child category")
    internal inner class ChildCategoryGiven {

        private lateinit var savedChildCategory: Category

        @BeforeEach
        fun setUp(context: VertxTestContext) {
            runBlocking {
                savedChildCategory = categoryRepositoryEBProxy.save(randomChildCategory()).awaitFirst()
                context.completeNow()
            }
        }

        @Nested
        @DisplayName("when query by parentId")
        internal inner class FindByParent {

            @Test
            @DisplayName("then child category with parentId should get returned")
            fun shouldReturnParentIdChild() {
                StepVerifier.create(categoryRepositoryEBProxy.findByParentId(savedChildCategory.parentId()!!))
                    .expectNext(savedChildCategory)
                    .verifyComplete()
            }

        }

    }

}