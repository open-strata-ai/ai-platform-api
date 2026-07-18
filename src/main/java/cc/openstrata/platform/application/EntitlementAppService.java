package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.SetEntitlementsRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.EntitlementConsistencyRule;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.BillingEventPort;
import cc.openstrata.platform.domain.port.ManifestPort;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Component whitelist (entitlement) use cases. */
public class EntitlementAppService {
    private final TenantRepository tenantRepository;
    private final EntitlementConsistencyRule entitlementConsistencyRule;
    private final ManifestPort manifestPort;
    private final BillingEventPort billingEventPort;
    private final AuditRecorder auditRecorder;

    public EntitlementAppService(TenantRepository tenantRepository,
                                 EntitlementConsistencyRule entitlementConsistencyRule,
                                 ManifestPort manifestPort, BillingEventPort billingEventPort,
                                 AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.entitlementConsistencyRule = entitlementConsistencyRule;
        this.manifestPort = manifestPort;
        this.billingEventPort = billingEventPort;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void setEntitlements(String tenantId, SetEntitlementsRequest req) {
        Tenant t = load(tenantId);
        Set<String> enabled = req.entitlements().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        entitlementConsistencyRule.validate(enabled);
        req.entitlements().forEach((k, v) -> t.setEntitlement(k, v));
        tenantRepository.save(t);
        manifestPort.enable(tenantId, "entitlements");
        billingEventPort.tenantEntitlementChanged(tenantId);
        auditRecorder.record(tenantId, actor(), "ENTITLEMENTS_SET", "{}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
