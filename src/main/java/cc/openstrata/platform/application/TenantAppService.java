package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.CreateTenantRequest;
import cc.openstrata.platform.application.dto.TenantResponse;
import cc.openstrata.platform.config.OpenstrataProperties;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.MultitenancyProvisioningRule;
import cc.openstrata.platform.domain.rule.TenantIdUniquenessRule;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.AuthPort;
import cc.openstrata.platform.domain.port.ControlPlaneClient;
import cc.openstrata.platform.domain.port.ManifestPort;
import cc.openstrata.platform.domain.port.MultiTenancyPort;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.UUID;

/** Tenant lifecycle use cases (create/suspend/resume/delete/enableMultitenancy). */
public class TenantAppService {
    private final TenantRepository tenantRepository;
    private final TenantIdUniquenessRule tenantIdUniquenessRule;
    private final AuthPort authPort;
    private final MultiTenancyPort multiTenancyPort;
    private final ManifestPort manifestPort;
    private final AuditRecorder auditRecorder;
    private final OpenstrataProperties properties;
    private final ControlPlaneClient controlPlaneClient;

    public TenantAppService(TenantRepository tenantRepository, TenantIdUniquenessRule tenantIdUniquenessRule,
                            AuthPort authPort, MultiTenancyPort multiTenancyPort, ManifestPort manifestPort,
                            AuditRecorder auditRecorder, OpenstrataProperties properties,
                            ControlPlaneClient controlPlaneClient) {
        this.tenantRepository = tenantRepository;
        this.tenantIdUniquenessRule = tenantIdUniquenessRule;
        this.authPort = authPort;
        this.multiTenancyPort = multiTenancyPort;
        this.manifestPort = manifestPort;
        this.auditRecorder = auditRecorder;
        this.properties = properties;
        this.controlPlaneClient = controlPlaneClient;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public TenantResponse createTenant(CreateTenantRequest req) {
        TenantId id = new TenantId(UUID.randomUUID().toString());
        tenantIdUniquenessRule.validate(id);
        Tenant t = Tenant.create(id, req.name());
        tenantRepository.save(t);
        authPort.createRealm(id.value());
        auditRecorder.record(id.value(), actor(), "TENANT_CREATED", "{\"name\":\"" + req.name() + "\"}");
        return toResponse(t);
    }

    public TenantResponse getTenant(String tenantId) {
        return toResponse(load(tenantId));
    }

    public TenantResponse suspendTenant(String tenantId) {
        Tenant t = load(tenantId);
        t.suspend();
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "TENANT_SUSPENDED", "{}");
        return toResponse(t);
    }

    public TenantResponse resumeTenant(String tenantId) {
        Tenant t = load(tenantId);
        t.resume();
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "TENANT_RESUMED", "{}");
        return toResponse(t);
    }

    public TenantResponse deleteTenant(String tenantId) {
        Tenant t = load(tenantId);
        t.markDeleted();
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "TENANT_DELETED", "{}");
        return toResponse(t);
    }

    public TenantResponse enableMultitenancy(String tenantId) {
        Tenant t = load(tenantId);
        String auth = properties.getSpi().getAuth();
        boolean authEnabled = auth != null && !"none".equals(auth);
        new MultitenancyProvisioningRule().validate(true, authEnabled);
        t.enableMultitenancy();
        multiTenancyPort.createTenant(tenantId);
        manifestPort.enable(tenantId, "multitenancy");
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "TENANT_MULTITENANCY_ENABLED", "{}");
        return toResponse(t);
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND,
                        "Tenant " + tenantId + " not found"));
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(t.getTenantId().value(), t.getName(), t.getStatus().name(),
                t.getPlanId() == null ? null : t.getPlanId().value(), t.isMultitenancyEnabled());
    }
}
