package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.SetEntitlementsRequest;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.rule.EntitlementConsistencyRule;
import com.openstrata.platform.infrastructure.adapter.InMemoryAuditRecorder;
import com.openstrata.platform.infrastructure.adapter.InMemoryBillingEventAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryManifestAdapter;
import com.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntitlementAppServiceTest {
    private final InMemoryTenantRepository repo = new InMemoryTenantRepository();
    private final InMemoryManifestAdapter manifest = new InMemoryManifestAdapter();
    private final InMemoryBillingEventAdapter billing = new InMemoryBillingEventAdapter();
    private final InMemoryAuditRecorder audit = new InMemoryAuditRecorder();
    private final EntitlementAppService service = new EntitlementAppService(repo,
            new EntitlementConsistencyRule(), manifest, billing, audit);

    private String seeded() {
        String id = "t1";
        repo.save(Tenant.create(new TenantId(id), "Acme"));
        return id;
    }

    @Test
    void validChainPersistsAndNotifiesBilling() {
        String id = seeded();
        service.setEntitlements(id, new SetEntitlementsRequest(
                Map.of("auth", true, "multitenancy", true, "billing", true)));
        Tenant t = repo.findById(new TenantId(id)).orElseThrow();
        assertTrue(t.getEntitlements().stream()
                .anyMatch(e -> e.getComponent().equals("billing") && e.isAllowed()));
        assertTrue(billing.entitlementChanges.contains(id));
    }

    @Test
    void billingWithoutMultitenancyRejected() {
        String id = seeded();
        assertThrows(DomainException.class, () -> service.setEntitlements(id,
                new SetEntitlementsRequest(Map.of("billing", true))));
    }
}
