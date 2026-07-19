package cc.openstrata.platform.domain;

import cc.openstrata.platform.domain.port.ModelRegistryPort;
import cc.openstrata.platform.infrastructure.adapter.InMemoryModelRegistryAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelWhitelistServiceTest {
    private final ModelRegistryPort reg = new InMemoryModelRegistryAdapter();
    private final ModelWhitelistService svc = new ModelWhitelistService(reg);

    @Test
    void authorizedWhenRegistered() {
        ((InMemoryModelRegistryAdapter) reg).register("qwen-max");
        assertTrue(svc.isAuthorized("t1", "qwen-max"));
    }

    @Test
    void notAuthorizedWhenMissing() {
        assertFalse(svc.isAuthorized("t1", "gpt-4"));
    }

    @Test
    void requireAuthorizedThrowsWhenMissing() {
        assertThrows(DomainException.class, () -> svc.requireAuthorized("t1", "gpt-4"));
    }
}
