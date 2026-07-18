package cc.openstrata.platform.domain.port;

import cc.openstrata.platform.domain.Role;

/** Auth SPI (Keycloak). ACL: external token/claims -> internal TenantContext/Role. */
public interface AuthPort {
    void createRealm(String tenantId);

    void createUser(String tenantId, String email, Role role);

    void syncRole(String userId, Role role);
}
