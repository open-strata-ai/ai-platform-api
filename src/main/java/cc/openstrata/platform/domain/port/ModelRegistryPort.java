package cc.openstrata.platform.domain.port;

import java.util.List;

/** Model directory SPI (PA-06 model authorization, DV-05 model binding). */
public interface ModelRegistryPort {
    List<String> listAvailableModels(String tenantId);

    void assignWhitelist(String tenantId, List<String> modelIds);
}
