package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.infrastructure.rest.webflux.model.PostCategoryRequest;
import com.github.javafaker.Faker;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.text.Normalizer.Form.NFD;

final class RandomDataProvider {

    private static final Faker FAKER = new Faker();

    static PostCategoryRequest randomPostCategoryRequest() {
        final String name = FAKER.commerce().department();

        return new PostCategoryRequest(
                name,
                SlugHelper.slugify(name),
                FAKER.bool().bool()
        );
    }

    static PostCategoryRequest randomPostCategoryRequestWithName(String name) {
        var randomPostRootCategoryRequest = randomPostCategoryRequest();

        return new PostCategoryRequest(
                name,
                randomPostRootCategoryRequest.slug(),
                randomPostRootCategoryRequest.isVisible()
        );
    }

    static PostCategoryRequest randomPostCategoryRequestWithSlug(String slug) {
        var randomPostRootCategoryRequest = randomPostCategoryRequest();

        return new PostCategoryRequest(
                randomPostRootCategoryRequest.name(),
                slug,
                randomPostRootCategoryRequest.isVisible()
        );
    }

    static String randomUUID() {
        return FAKER.internet().uuid();
    }

    //TODO centralize
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
