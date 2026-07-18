package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.RegisterApplicationRequest;
import com.openstrata.platform.config.TenantContext;
import com.openstrata.platform.domain.AgentSpecRef;
import com.openstrata.platform.domain.AppId;
import com.openstrata.platform.domain.Application;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.port.AppRegistryPort;
import com.openstrata.platform.domain.port.AuditRecorder;
import com.openstrata.platform.domain.port.TenantRepository;
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
