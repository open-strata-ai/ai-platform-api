package cc.openstrata.platform.domain.port;

import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import java.util.List;
import java.util.Optional;

/** Persistence port for the Tenant aggregate (in-memory for offline; JPA in prod). */
public interface TenantRepository {
    void save(Tenant tenant);

    Optional<Tenant> findById(TenantId id);

    boolean exists(TenantId id);

    List<Tenant> findAll();
}
