package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory TenantRepository used by offline tests and as a default runtime. */
public class InMemoryTenantRepository implements TenantRepository {
    private final Map<String, Tenant> store = new ConcurrentHashMap<>();

    @Override
    public void save(Tenant tenant) {
        store.put(tenant.getTenantId().value(), tenant);
    }

    @Override
    public Optional<Tenant> findById(TenantId id) {
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public boolean exists(TenantId id) {
        return store.containsKey(id.value());
    }

    @Override
    public List<Tenant> findAll() {
        return List.copyOf(store.values());
    }
}
