package codes.fdk.blueprint.api.domain.service;

import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static codes.fdk.blueprint.api.domain.RandomDataProvider.randomCategoryWithId;
import static codes.fdk.blueprint.api.domain.RandomDataProvider.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DefaultCategoryService categoryService;

    @Nested
    @DisplayName("Given the category for id doesn't exist")
    class CategoryDoesNotExist {

        @BeforeEach
        void setUp() {
            when(categoryRepository.findById(any(CategoryId.class))).thenReturn(Mono.empty());
        }

        @Test
        @DisplayName("then update should return nothing")
        public void nothingShouldGetReturned() {
            StepVerifier.create(categoryService.update(new UpdateCategoryCommand(CategoryId.of(randomUUID()), true)))
                        .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Given the category for id does exist")
    class CategoryDoesExist {

        private Category categoryToUpdate;

        @BeforeEach
        void setUp() {
            categoryToUpdate = randomCategoryWithId();
            when(categoryRepository.findById(any(CategoryId.class))).thenReturn(Mono.just(categoryToUpdate));
            when(categoryRepository.save(any(Category.class))).thenAnswer(a -> Mono.just(a.getArgument(0)));
        }

        @Test
        @DisplayName("then update should invoke the repository save method with category containing updated properties")
        void updateShouldInvokeRepoSaveWithUpdatedProperties() {
            final UpdateCategoryCommand updateCommand = new UpdateCategoryCommand(
                    categoryToUpdate.id(),
                    !categoryToUpdate.visible()
            );

            StepVerifier.create(categoryService.update(updateCommand))
                        .expectNextCount(1)
                        .verifyComplete();

            verify(categoryRepository).save(eq(new Category(
                    categoryToUpdate.id(),
                    categoryToUpdate.name(),
                    categoryToUpdate.slug(),
                    categoryToUpdate.parentId(),
                    updateCommand.visible()
            )));
        }

    }

}