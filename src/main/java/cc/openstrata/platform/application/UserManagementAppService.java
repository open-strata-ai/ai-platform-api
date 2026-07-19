package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.ChangeRoleRequest;
import cc.openstrata.platform.application.dto.InviteUserRequest;
import cc.openstrata.platform.application.dto.UserView;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** User management use cases for the Agent platform (TA-01, TA-02, TA-10). */
public class UserManagementAppService {
    private final TenantRepository tenantRepository;
    private final AuthPort authPort;
    private final AuditRecorder auditRecorder;

    public UserManagementAppService(TenantRepository tenantRepository, AuthPort authPort,
                                    AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.authPort = authPort;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public UserView inviteUser(String tenantId, InviteUserRequest req) {
        Tenant t = load(tenantId);
        UserId uid = new UserId(UUID.randomUUID().toString());
        User u = new User(uid, new TenantId(tenantId), req.email(), Role.fromCode(req.role()));
        t.addUser(u);
        tenantRepository.save(t);
        authPort.createUser(tenantId, req.email(), u.getRole());
        auditRecorder.record(tenantId, actor(), "USER_INVITED", "{\"email\":\"" + req.email() + "\"}");
        return toView(u);
    }

    public UserView changeUserRole(String tenantId, String userId, ChangeRoleRequest req) {
        Tenant t = load(tenantId);
        Role role = Role.fromCode(req.role());
        t.changeUserRole(new UserId(userId), role);
        tenantRepository.save(t);
        authPort.syncRole(userId, role);
        return toView(t.findUser(new UserId(userId)));
    }

    public void deactivateUser(String tenantId, String userId) {
        Tenant t = load(tenantId);
        t.findUser(new UserId(userId)).disable();
        tenantRepository.save(t);
    }

    public List<UserView> listTenantUsers(String tenantId) {
        Tenant t = load(tenantId);
        return t.getUsers().stream().map(this::toView).collect(Collectors.toList());
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }

    private UserView toView(User u) {
        return new UserView(u.getUserId().value(), u.getEmail(), u.getRole().getCode(), u.getStatus().name());
    }
}
