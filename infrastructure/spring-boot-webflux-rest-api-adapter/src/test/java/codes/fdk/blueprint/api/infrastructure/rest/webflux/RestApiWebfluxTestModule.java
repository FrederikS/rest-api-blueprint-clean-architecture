package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.service.CategoryService;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.R2dbcPersistenceModule;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(R2dbcPersistenceModule.class)
public class RestApiWebfluxTestModule {

    @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

}
