package cc.openstrata.platform.domain.port;

import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import java.util.List;
import java.util.Optional;

/** Predefined plan tiers (Trial/Standard/Enterprise). */
public interface PlanRepository {
    Optional<Plan> findById(PlanId id);

    List<Plan> all();
}
