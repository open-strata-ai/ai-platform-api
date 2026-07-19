package cc.openstrata.platform.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pure-logic tests for the tenant-id sanitization used by RlsTransactionManager. */
class RlsTransactionManagerTest {

    @Test
    void acceptsSafeTenantIds() {
        assertTrue(RlsTransactionManager.isValidTenantId("local"));
        assertTrue(RlsTransactionManager.isValidTenantId("tenant-123"));
        assertTrue(RlsTransactionManager.isValidTenantId("acme_corp.v2"));
        assertTrue(RlsTransactionManager.isValidTenantId("T9-x.y_z"));
    }

    @Test
    void rejectsUnsafeTenantIds() {
        assertFalse(RlsTransactionManager.isValidTenantId(null));
        assertFalse(RlsTransactionManager.isValidTenantId(""));
        assertFalse(RlsTransactionManager.isValidTenantId("a'b"));
        assertFalse(RlsTransactionManager.isValidTenantId("drop table"));
        assertFalse(RlsTransactionManager.isValidTenantId("tenant;DELETE"));
        assertFalse(RlsTransactionManager.isValidTenantId("x".repeat(65)));
    }
}
