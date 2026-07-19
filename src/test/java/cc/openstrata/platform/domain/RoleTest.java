package cc.openstrata.platform.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RoleTest {
    @Test
    void consumerRoleIsPresent() {
        assertEquals(Role.CONSUMER, Role.fromCode("consumer"));
        assertEquals("consumer", Role.CONSUMER.getCode());
    }

    @Test
    void allLegacyRolesResolve() {
        assertEquals(Role.PLATFORM_ADMIN, Role.fromCode("platform-admin"));
        assertEquals(Role.TENANT_ADMIN, Role.fromCode("tenant-admin"));
        assertEquals(Role.DEVELOPER, Role.fromCode("developer"));
        assertEquals(Role.VIEWER, Role.fromCode("viewer"));
    }

    @Test
    void unknownRoleThrows() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromCode("nope"));
    }
}
