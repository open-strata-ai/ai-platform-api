package cc.openstrata.platform.application;

import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ModelWhitelistService;
import cc.openstrata.platform.domain.port.ModelRegistryPort;
import cc.openstrata.platform.infrastructure.adapter.InMemoryModelRegistryAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelAuthorizationAppServiceTest {
    private final ModelRegistryPort reg = new InMemoryModelRegistryAdapter();
    private final ModelAuthorizationAppService svc =
        new ModelAuthorizationAppService(new ModelWhitelistService(reg), reg);

    @BeforeEach
    void setUp() {
        TenantContext.set(TenantContext.of("t1", "u1", "developer"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void checkPassesWhenRegistered() {
        ((InMemoryModelRegistryAdapter) reg).register("qwen-max");
        assertDoesNotThrow(() -> svc.check("qwen-max"));
    }

    @Test
    void checkFailsWhenNotRegistered() {
        assertThrows(DomainException.class, () -> svc.check("gpt-4"));
    }
}
