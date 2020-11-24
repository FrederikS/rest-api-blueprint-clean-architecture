package codes.fdk.blueprint.api.application.spring;

import codes.fdk.blueprint.api.domain.service.CategoryService;
import codes.fdk.blueprint.api.domain.spi.CategoryRepository;
import codes.fdk.blueprint.api.infrastructure.persistence.r2dbc.R2dbcPersistenceModule;
import codes.fdk.blueprint.api.infrastructure.rest.webflux.RestApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({R2dbcPersistenceModule.class, RestApiConfig.class})
public class CategoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategoryApiApplication.class, args);
    }

    @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return CategoryService.create(categoryRepository);
    }

}
