package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.Set;
import java.util.UUID;

@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableR2dbcRepositories
public class R2dbcPersistenceModule {

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        final ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }

    @Bean
    R2dbcCustomConversions customConversions() {
        return new R2dbcCustomConversions(Set.of(new CategoryIdReadingConverter(), new CategoryIdWritingConverter()));
    }

    @ReadingConverter
    static class CategoryIdReadingConverter implements Converter<UUID, CategoryId> {

        @Override
        public CategoryId convert(UUID uuid) {
            return CategoryId.of(uuid.toString());
        }

    }

    @WritingConverter
    static class CategoryIdWritingConverter implements Converter<CategoryId, UUID> {

        @Override
        public UUID convert(CategoryId id) {
            return UUID.fromString(id.value());
        }

    }

}
