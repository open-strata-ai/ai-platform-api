package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.port.ManifestPort;
import java.util.ArrayList;
import java.util.List;

/** Offline ManifestPort adapter — records enabled capabilities. */
public class InMemoryManifestAdapter implements ManifestPort {
    public final List<String> enabled = new ArrayList<>();

    @Override
    public void enable(String tenantId, String capability) {
        enabled.add(tenantId + ":" + capability);
    }
}
