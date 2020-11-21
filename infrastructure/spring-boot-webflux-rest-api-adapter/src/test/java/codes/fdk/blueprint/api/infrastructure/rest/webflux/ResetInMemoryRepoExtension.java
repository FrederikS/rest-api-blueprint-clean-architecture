package codes.fdk.blueprint.api.infrastructure.rest.webflux;

import codes.fdk.blueprint.api.infrastructure.rest.webflux.RestApiWebfluxTestModule.InMemoryCategoryRepository;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ResetInMemoryRepoExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        InMemoryCategoryRepository.STORE.clear();
    }

}
