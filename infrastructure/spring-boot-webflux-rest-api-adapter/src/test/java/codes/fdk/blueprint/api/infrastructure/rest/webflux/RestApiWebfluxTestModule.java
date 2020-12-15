package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.service.CategoryService;
import codes.fdk.blueprint.api.domain.stub.InMemoryCategoryRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestApiWebfluxTestModule {

    @Bean
    public CategoryService categoryService() {
        return CategoryService.create(new InMemoryCategoryRepository());
    }

}
