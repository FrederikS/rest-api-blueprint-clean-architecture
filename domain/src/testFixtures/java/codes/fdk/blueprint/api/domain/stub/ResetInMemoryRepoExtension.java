package codes.fdk.blueprint.api.domain.stub;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ResetInMemoryRepoExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        InMemoryCategoryRepository.STORE.clear();
    }

}
