package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import com.github.javafaker.Faker;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.text.Normalizer.Form.NFD;

public class RandomDataProvider {

    private static final Faker FAKER = new Faker();

    static CategoryEntity randomCategory() {
        final String name = FAKER.commerce().department();

        return new CategoryEntity(
                null,
                name,
                SlugHelper.slugify(name),
                null,
                FAKER.bool().bool()
        );
    }

    static CategoryEntity randomCategoryWithId() {
        final CategoryEntity randomEntity = randomCategory();

        return new CategoryEntity(
                CategoryId.of(FAKER.internet().uuid()),
                randomEntity.name(),
                randomEntity.slug(),
                randomEntity.parentId(),
                randomEntity.visible()
        );
    }

    static CategoryEntity randomChildCategory() {
        final CategoryEntity randomEntity = randomCategory();

        return new CategoryEntity(
                randomEntity.id(),
                randomEntity.name(),
                randomEntity.slug(),
                CategoryId.of(FAKER.internet().uuid()),
                randomEntity.visible()
        );
    }

    static String randomUUID() {
        return FAKER.internet().uuid();
    }

    private static final class SlugHelper {

        private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
        private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

        public static String slugify(String input) {
            String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
            String normalized = Normalizer.normalize(noWhitespace, NFD);
            String slug = NONLATIN.matcher(normalized).replaceAll("");
            return slug.toLowerCase(Locale.ENGLISH);
        }

    }

}
