package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.port.ControlPlaneClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Offline ControlPlaneClient adapter — records published snapshots. */
public class InMemoryControlPlaneClient implements ControlPlaneClient {
    public final List<String> snapshots = new ArrayList<>();

    @Override
    public void publishTenantSnapshot(String tenantId, Map<String, Object> snapshot) {
        snapshots.add(tenantId);
    }
}
