package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.RandomDataProvider.randomCategory;
import static codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.RandomDataProvider.randomCategoryWithId;
import static codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.RandomDataProvider.randomChildCategory;
import static codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.RandomDataProvider.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@IntegrationTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryEntityRepository categoryRepository;

    @Nested
    @DisplayName("When findById for non-existent ID")
    class FindById {

        @Test
        @DisplayName("then empty Mono should get returned")
        void shouldReturnEmptyMono() {
            StepVerifier.create(categoryRepository.findById(CategoryId.of(randomUUID())))
                        .verifyComplete();
        }

    }

    @Nested
    @DisplayName("When upsert a category without id")
    class UpsertCategoryWithoutId {

        @Test
        @DisplayName("then saved category should get returned")
        void shouldReturnSavedCategory() {
            final CategoryEntity categoryToSave = randomCategory();

            StepVerifier.create(categoryRepository.save(categoryToSave))
                        .assertNext(category -> {
                            assertThat(category).returns(categoryToSave.name(), from(CategoryEntity::name))
                                                .returns(categoryToSave.slug(), from(CategoryEntity::slug))
                                                .returns(categoryToSave.parentId(), from(CategoryEntity::parentId))
                                                .returns(categoryToSave.visible(), from(CategoryEntity::visible));
                        })
                        .verifyComplete();
        }

        @Test
        @DisplayName("then saved category should get an id assigned")
        void shouldGetAnId() {
            final CategoryEntity categoryToSave = randomCategory();
            assertThat(categoryToSave.id()).isNull();

            StepVerifier.create(categoryRepository.save(categoryToSave))
                        .assertNext(savedCategory -> assertThat(savedCategory.id()).isNotNull()
                                                                                   .extracting(CategoryId::value)
                                                                                   .asString()
                                                                                   .isNotBlank())
                        .verifyComplete();
        }

    }

    @Nested
    @DisplayName("When save a category with non-existent id")
    class UpsertCategoryWithId {

        @Test
        @DisplayName("then save should return an error")
        void shouldReturnSavedCategory() {
            final CategoryEntity categoryToSave = randomCategoryWithId();

            StepVerifier.create(categoryRepository.save(categoryToSave))
                        .verifyError();
        }

    }

    @Nested
    @DisplayName("When query all entities on empty repository")
    class FindAll {

        @Test
        @DisplayName("then nothing should get returned")
        public void shouldReturnEmptyPublisher() {
            StepVerifier.create(categoryRepository.findAll())
                        .verifyComplete();
        }

    }

    @Nested
    @DisplayName("When query by non-existent parentId")
    class FindByParent {

        @Test
        @DisplayName("then nothing should get returned")
        public void shouldReturnEmptyPublisher() {
            StepVerifier.create(categoryRepository.findByParentId(CategoryId.of(randomUUID())))
                        .verifyComplete();
        }

    }

    @Nested
    @DisplayName("Given a saved category")
    class CategoryGiven {

        private CategoryEntity savedCategory;

        @BeforeEach
        void setUp() {
            savedCategory = categoryRepository.save(randomCategory())
                                              .block();
        }

        @Nested
        @DisplayName("when findById for saved category")
        class FindById {

            @Test
            @DisplayName("then saved category should get returned")
            void shouldReturnCategory() {
                StepVerifier.create(categoryRepository.findById(savedCategory.id()))
                            .expectNext(savedCategory)
                            .verifyComplete();
            }

        }

        @Nested
        @DisplayName("when findAll")
        class FindAll {

            @Test
            @DisplayName("then all saved categories should get returned")
            void shouldReturnAllCategories() {
                StepVerifier.create(categoryRepository.findAll())
                            .expectNext(savedCategory)
                            .verifyComplete();
            }

        }

    }

    @Nested
    @DisplayName("Given a saved child category")
    class ChildCategoryGiven {

        private CategoryEntity savedChildCategory;

        @BeforeEach
        void setUp() {
            savedChildCategory = categoryRepository.save(randomChildCategory())
                                                   .block();
        }

        @Nested
        @DisplayName("when query by parentId")
        class FindByParent {

            @Test
            @DisplayName("then child category with parentId should get returned")
            void shouldReturnParentIdChild() {
                StepVerifier.create(categoryRepository.findByParentId(savedChildCategory.parentId()))
                            .expectNext(savedChildCategory)
                            .verifyComplete();
            }
        }

    }

}