package codes.fdk.blueprint.api.domain;

import codes.fdk.blueprint.api.domain.model.Category;
import codes.fdk.blueprint.api.domain.model.CategoryId;
import com.github.javafaker.Faker;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.text.Normalizer.Form.NFD;

public class RandomDataProvider {

    private static final Faker FAKER = new Faker();

    public static Category randomCategory() {
        final String name = FAKER.commerce().department();

        return new Category(
                null,
                name,
                SlugHelper.slugify(name),
                null,
                FAKER.bool().bool()
        );
    }

    public static Category randomCategoryWithId() {
        final Category randomCategory = randomCategory();

        return new Category(
                CategoryId.of(FAKER.internet().uuid()),
                randomCategory.name(),
                randomCategory.slug(),
                randomCategory.parentId(),
                randomCategory.isVisible()
        );
    }

    public static Category randomChildCategory() {
        final Category randomCategory = randomCategory();

        return new Category(
                randomCategory.id(),
                randomCategory.name(),
                randomCategory.slug(),
                randomId(),
                randomCategory.isVisible()
        );
    }

    public static String randomUUID() {
        return FAKER.internet().uuid();
    }

    public static CategoryId randomId() {
        return CategoryId.of(randomUUID());
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
