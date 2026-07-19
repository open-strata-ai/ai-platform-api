package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.ModelRegistryPort;
import java.util.ArrayList;
import java.util.List;

/** In-memory ModelRegistryPort stand-in for Batch C tests (DV-05, PA-06). */
public class InMemoryModelRegistryAdapter implements ModelRegistryPort {
    private final List<String> models = new ArrayList<>();
    private final List<String> whitelist = new ArrayList<>();

    public void register(String modelId) {
        models.add(modelId);
    }

    @Override
    public List<String> listAvailableModels(String tenantId) {
        return new ArrayList<>(models);
    }

    @Override
    public void assignWhitelist(String tenantId, List<String> modelIds) {
        whitelist.clear();
        whitelist.addAll(modelIds);
    }

    public List<String> getWhitelist() {
        return new ArrayList<>(whitelist);
    }
}
