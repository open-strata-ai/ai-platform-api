package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.ModelRegistryPort;
import cc.openstrata.platform.infrastructure.persistence.ModelWhitelistEntity;
import cc.openstrata.platform.infrastructure.persistence.ModelWhitelistJpaRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Production {@link ModelRegistryPort}. There is no external model-registry
 * service; the model catalog/whitelist is a platform-api aggregate persisted in
 * the {@code model_whitelist} table.
 */
public class JpaModelRegistryAdapter implements ModelRegistryPort {

    private final ModelWhitelistJpaRepository repo;

    public JpaModelRegistryAdapter(ModelWhitelistJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<String> listAvailableModels(String tenantId) {
        List<String> models = new ArrayList<>();
        for (ModelWhitelistEntity e : repo.findByTenantId(tenantId)) {
            models.add(e.getModel());
        }
        return models;
    }

    @Override
    public void assignWhitelist(String tenantId, List<String> modelIds) {
        repo.deleteByTenantId(tenantId);
        for (String m : modelIds) {
            ModelWhitelistEntity e = new ModelWhitelistEntity();
            e.setTenantId(tenantId);
            e.setModel(m);
            repo.save(e);
        }
    }
}
