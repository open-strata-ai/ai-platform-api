package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.CreateTenantRequest;
import cc.openstrata.platform.application.dto.TenantResponse;
import cc.openstrata.platform.config.OpenstrataProperties;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.TenantIdUniquenessRule;
import cc.openstrata.platform.infrastructure.adapter.InMemoryAuditRecorder;
import cc.openstrata.platform.infrastructure.adapter.InMemoryAuthAdapter;
import cc.openstrata.platform.infrastructure.adapter.InMemoryControlPlaneClient;
import cc.openstrata.platform.infrastructure.adapter.InMemoryManifestAdapter;
import cc.openstrata.platform.infrastructure.adapter.InMemoryMultiTenancyAdapter;
import cc.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantAppServiceTest {
    private final InMemoryTenantRepository repo = new InMemoryTenantRepository();
    private final InMemoryAuthAdapter auth = new InMemoryAuthAdapter();
    private final InMemoryMultiTenancyAdapter mt = new InMemoryMultiTenancyAdapter();
    private final InMemoryManifestAdapter manifest = new InMemoryManifestAdapter();
    private final InMemoryAuditRecorder audit = new InMemoryAuditRecorder();
    private final InMemoryControlPlaneClient cp = new InMemoryControlPlaneClient();
    private final OpenstrataProperties props = new OpenstrataProperties();
    private final TenantAppService service = new TenantAppService(repo,
            new TenantIdUniquenessRule(repo), auth, mt, manifest, audit, props, cp);

    @Test
    void createTriggersAuthRealmAndAudit() {
        TenantResponse resp = service.createTenant(new CreateTenantRequest("Acme"));
        assertEquals("Acme", resp.name());
        assertEquals("PROVISIONING", resp.status());
        assertTrue(auth.realms.contains(resp.tenantId()));
        assertEquals(1, audit.entries().size());
        assertEquals("TENANT_CREATED", audit.entries().get(0).getAction());
    }

    @Test
    void enableMultitenancyRequiresAuthAndProvisions() {
        TenantResponse resp = service.createTenant(new CreateTenantRequest("Acme"));
        TenantResponse enabled = service.enableMultitenancy(resp.tenantId());
        assertTrue(enabled.multitenancyEnabled());
        assertTrue(mt.created.contains(resp.tenantId()));
        assertTrue(manifest.enabled.contains(resp.tenantId() + ":multitenancy"));
    }

    @Test
    void suspendThenGet() {
        TenantResponse resp = service.createTenant(new CreateTenantRequest("Acme"));
        service.suspendTenant(resp.tenantId());
        assertEquals("SUSPENDED", service.getTenant(resp.tenantId()).status());
    }

    @Test
    void unknownTenantThrows() {
        assertThrows(DomainException.class, () -> service.getTenant("missing"));
    }
}
