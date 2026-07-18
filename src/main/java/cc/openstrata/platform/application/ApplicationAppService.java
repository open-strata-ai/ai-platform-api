package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.RegisterApplicationRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.AgentSpecRef;
import cc.openstrata.platform.domain.AppId;
import cc.openstrata.platform.domain.Application;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.port.AppRegistryPort;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.UUID;

/** Application (Agent App) registry use cases. */
public class ApplicationAppService {
    private final TenantRepository tenantRepository;
    private final AppRegistryPort appRegistryPort;
    private final AuditRecorder auditRecorder;

    public ApplicationAppService(TenantRepository tenantRepository, AppRegistryPort appRegistryPort,
                                 AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.appRegistryPort = appRegistryPort;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void registerApplication(String tenantId, RegisterApplicationRequest req) {
        Tenant t = load(tenantId);
        AppId aid = new AppId(UUID.randomUUID().toString());
        Application app = new Application(aid, new TenantId(tenantId), req.name(),
                AgentSpecRef.parse(req.agentSpecRef()));
        t.registerApplication(app);
        tenantRepository.save(t);
        appRegistryPort.registerApplication(tenantId, aid.value(), req.name());
        auditRecorder.record(tenantId, actor(), "APPLICATION_REGISTERED", "{\"appId\":\"" + aid.value() + "\"}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
