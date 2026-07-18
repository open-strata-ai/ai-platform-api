package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.BillingEventPort;
import java.util.ArrayList;
import java.util.List;

/** Offline BillingEventPort (ai-billing-service) adapter. */
public class InMemoryBillingEventAdapter implements BillingEventPort {
    public final List<String> entitlementChanges = new ArrayList<>();
    public final List<String> quotaChanges = new ArrayList<>();

    @Override
    public void tenantEntitlementChanged(String tenantId) {
        entitlementChanges.add(tenantId);
    }

    @Override
    public void quotaChanged(String tenantId) {
        quotaChanges.add(tenantId);
    }
}
