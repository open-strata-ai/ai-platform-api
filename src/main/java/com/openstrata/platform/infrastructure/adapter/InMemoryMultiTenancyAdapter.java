package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.port.MultiTenancyPort;
import java.util.ArrayList;
import java.util.List;

/** Offline MultiTenancyPort (Capsule) adapter — null-object when disabled. */
public class InMemoryMultiTenancyAdapter implements MultiTenancyPort {
    public final List<String> created = new ArrayList<>();
    public final List<String> deleted = new ArrayList<>();

    @Override
    public void createTenant(String tenantId) {
        created.add(tenantId);
    }

    @Override
    public void deleteTenant(String tenantId) {
        deleted.add(tenantId);
    }
}
