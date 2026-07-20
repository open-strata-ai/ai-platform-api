package cc.openstrata.platform.domain;

import cc.openstrata.platform.domain.port.ModelRegistryPort;
import java.util.List;

/** Enforces per-tenant model authorization (PA-06, DESIGN §4). */
public class ModelWhitelistService {
    private final ModelRegistryPort registry;

    public ModelWhitelistService(ModelRegistryPort registry) {
        this.registry = registry;
    }

    public void requireAuthorized(String tenantId, String modelId) {
        if (!isAuthorized(tenantId, modelId)) {
            throw new DomainException(ErrorCode.MODEL_NOT_AUTHORIZED,
                "model " + modelId + " not authorized for tenant " + tenantId);
        }
    }

    public boolean isAuthorized(String tenantId, String modelId) {
        return registry.listAvailableModels(tenantId).contains(modelId);
    }

    public List<String> available(String tenantId) {
        return registry.listAvailableModels(tenantId);
    }
}
