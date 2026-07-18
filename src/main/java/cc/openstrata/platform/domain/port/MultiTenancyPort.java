package cc.openstrata.platform.domain.port;

/** MultiTenancy SPI (Capsule). Null-object when multitenancy disabled. */
public interface MultiTenancyPort {
    void createTenant(String tenantId);

    void deleteTenant(String tenantId);
}
