package cc.openstrata.platform.domain.port;

/** Manifest SPI (config-driven, delivered via ai-dependency-resolver). */
public interface ManifestPort {
    void enable(String tenantId, String capability);
}
