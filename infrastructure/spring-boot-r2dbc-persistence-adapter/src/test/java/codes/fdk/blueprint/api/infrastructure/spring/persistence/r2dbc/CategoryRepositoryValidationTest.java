package codes.fdk.blueprint.api.infrastructure.spring.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
class CategoryRepositoryValidationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("When passing null entity to save")
    class Save {

        @Test
        @DisplayName("then an error should be thrown")
        void errorShouldBeThrown() {
            assertThrows(IllegalArgumentException.class, () -> categoryRepository.save(null));
        }

    }

    @Nested
    @DisplayName("When passing null id to findById")
    class FindByNullId {

        @Test
        @DisplayName("then an error should be thrown")
        void errorShouldBeThrown() {
            assertThrows(IllegalArgumentException.class, () -> categoryRepository.findById(null));
        }

    }

}
