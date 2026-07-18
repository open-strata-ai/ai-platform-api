package cc.openstrata.platform.domain.port;

/** Billing SPI (ai-billing-service). Multi-tenant only. */
public interface BillingEventPort {
    void tenantEntitlementChanged(String tenantId);

    void quotaChanged(String tenantId);
}
