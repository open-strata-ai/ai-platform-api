package cc.openstrata.platform.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MockDeploymentAdapterTest {
    @Test
    void deployMarksDeployed() {
        MockDeploymentAdapter a = new MockDeploymentAdapter();
        a.deploy("t1", "a1", "v1", "spec");
        assertEquals("DEPLOYED", a.getDeployStatus("a1", "v1"));
    }

    @Test
    void rollbackMarksRolledBack() {
        MockDeploymentAdapter a = new MockDeploymentAdapter();
        a.deploy("t1", "a1", "v1", "spec");
        a.rollback("a1", "v1");
        assertEquals("ROLLED_BACK", a.getDeployStatus("a1", "v1"));
    }
}
