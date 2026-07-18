package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.ChangeRoleRequest;
import cc.openstrata.platform.application.dto.InviteUserRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Role;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.User;
import cc.openstrata.platform.domain.UserId;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.AuthPort;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.UUID;

/** User identity use cases (invite/disable/changeRole). */
public class UserAppService {
    private final TenantRepository tenantRepository;
    private final AuthPort authPort;
    private final AuditRecorder auditRecorder;

    public UserAppService(TenantRepository tenantRepository, AuthPort authPort, AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.authPort = authPort;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void inviteUser(String tenantId, InviteUserRequest req) {
        Tenant t = load(tenantId);
        UserId uid = new UserId(UUID.randomUUID().toString());
        User u = new User(uid, new TenantId(tenantId), req.email(), Role.fromCode(req.role()));
        t.addUser(u);
        tenantRepository.save(t);
        authPort.createUser(tenantId, req.email(), u.getRole());
        auditRecorder.record(tenantId, actor(), "USER_INVITED", "{\"email\":\"" + req.email() + "\"}");
    }

    public void disableUser(String tenantId, String userId) {
        Tenant t = load(tenantId);
        t.findUser(new UserId(userId)).disable();
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "USER_DISABLED", "{\"userId\":\"" + userId + "\"}");
    }

    public void changeRole(String tenantId, String userId, ChangeRoleRequest req) {
        Tenant t = load(tenantId);
        Role role = Role.fromCode(req.role());
        t.changeUserRole(new UserId(userId), role);
        tenantRepository.save(t);
        authPort.syncRole(userId, role);
        auditRecorder.record(tenantId, actor(), "USER_ROLE_CHANGED", "{\"userId\":\"" + userId + "\"}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
