package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.Role;
import com.openstrata.platform.domain.port.AuthPort;
import java.util.ArrayList;
import java.util.List;

/** Offline AuthPort (Keycloak) adapter — records calls for assertions. */
public class InMemoryAuthAdapter implements AuthPort {
    public final List<String> realms = new ArrayList<>();
    public final List<String> users = new ArrayList<>();
    public final List<String> roles = new ArrayList<>();

    @Override
    public void createRealm(String tenantId) {
        realms.add(tenantId);
    }

    @Override
    public void createUser(String tenantId, String email, Role role) {
        users.add(tenantId + ":" + email);
    }

    @Override
    public void syncRole(String userId, Role role) {
        roles.add(userId + ":" + role.getCode());
    }
}
