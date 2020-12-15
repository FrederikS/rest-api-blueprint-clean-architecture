package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.domain.stub.InMemoryCategoryRepository;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ResetInMemoryRepoExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        InMemoryCategoryRepository.STORE.clear();
    }

}
