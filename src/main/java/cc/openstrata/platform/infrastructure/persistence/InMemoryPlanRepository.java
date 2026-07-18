package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.QuotaTemplate;
import cc.openstrata.platform.domain.port.PlanRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Seeded with the three predefined plan tiers (DESIGN §2). */
public class InMemoryPlanRepository implements PlanRepository {
    private final Map<String, Plan> plans = new ConcurrentHashMap<>();

    public InMemoryPlanRepository() {
        plans.put("trial", new Plan(new PlanId("trial"), "Trial",
                new QuotaTemplate(1, 1_000, 10, 0, 0), false));
        plans.put("standard", new Plan(new PlanId("standard"), "Standard",
                new QuotaTemplate(4, 10_000, 50, 1, 0), false));
        plans.put("enterprise", new Plan(new PlanId("enterprise"), "Enterprise",
                new QuotaTemplate(16, 100_000, 200, 10, 4), true));
    }

    @Override
    public Optional<Plan> findById(PlanId id) {
        return Optional.ofNullable(plans.get(id.value()));
    }

    @Override
    public List<Plan> all() {
        return List.copyOf(plans.values());
    }
}
