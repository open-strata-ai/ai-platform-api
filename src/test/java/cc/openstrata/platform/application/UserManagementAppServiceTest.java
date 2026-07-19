package cc.openstrata.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.platform.application.dto.ChangeRoleRequest;
import cc.openstrata.platform.application.dto.InviteUserRequest;
import cc.openstrata.platform.application.dto.UserView;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.Role;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.TenantRepository;
import cc.openstrata.platform.infrastructure.adapter.InMemoryAuthAdapter;
import cc.openstrata.platform.infrastructure.adapter.InMemoryAuthAdapter;
import cc.openstrata.platform.infrastructure.adapter.InMemoryAuditRecorder;
import cc.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserManagementAppServiceTest {
    private final TenantRepository tenantRepo = new InMemoryTenantRepository();
    private final InMemoryAuthAdapter auth = new InMemoryAuthAdapter();
    private final AuditRecorder audit = new InMemoryAuditRecorder();
    private final UserManagementAppService svc = new UserManagementAppService(tenantRepo, auth, audit);

    @BeforeEach
    void setup() {
        TenantContext.set(TenantContext.of("t1", "admin", "tenant-admin"));
        tenantRepo.save(new Tenant(new TenantId("t1"), "Acme"));
    }

    @Test
    void inviteUserCreatesAndProvisionsAuth() {
        UserView u = svc.inviteUser("t1", new InviteUserRequest("dev@acme.com", "developer"));
        assertEquals("developer", u.role());
        assertTrue(auth.users.contains("t1:dev@acme.com"));
        assertEquals(1, svc.listTenantUsers("t1").size());
    }

    @Test
    void changeUserRoleUpdatesRole() {
        UserView u = svc.inviteUser("t1", new InviteUserRequest("dev@acme.com", "developer"));
        UserView updated = svc.changeUserRole("t1", u.userId(), new ChangeRoleRequest("viewer"));
        assertEquals("viewer", updated.role());
        assertTrue(auth.roles.contains(u.userId() + ":viewer"));
    }

    @Test
    void deactivateUserDisables() {
        UserView u = svc.inviteUser("t1", new InviteUserRequest("dev@acme.com", "developer"));
        svc.deactivateUser("t1", u.userId());
        List<UserView> users = svc.listTenantUsers("t1");
        assertTrue(users.stream().anyMatch(x -> x.userId().equals(u.userId()) && "DISABLED".equals(x.status())));
    }
}
