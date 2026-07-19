package cc.openstrata.platform.application;

import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.ModelWhitelistService;
import cc.openstrata.platform.domain.port.ModelRegistryPort;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: model authorization for the current tenant (PA-06). */
@Service
public class ModelAuthorizationAppService {
    private final ModelWhitelistService whitelist;
    private final ModelRegistryPort registry;

    public ModelAuthorizationAppService(ModelWhitelistService whitelist, ModelRegistryPort registry) {
        this.whitelist = whitelist;
        this.registry = registry;
    }

    public void check(String modelId) {
        whitelist.requireAuthorized(TenantContext.get().getTenantId(), modelId);
    }

    public List<String> available() {
        return whitelist.available(TenantContext.get().getTenantId());
    }

    public void assignWhitelist(List<String> modelIds) {
        registry.assignWhitelist(TenantContext.get().getTenantId(), modelIds);
    }
}
