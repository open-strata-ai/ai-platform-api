package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.QuotaTemplate;
import cc.openstrata.platform.domain.port.PlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** JPA-backed PlanRepository. Activated by @Profile("prod"). */
public class JpaPlanRepository implements PlanRepository {

    private final PlanJpaRepository repo;
    private final ObjectMapper mapper;

    public JpaPlanRepository(PlanJpaRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<Plan> findById(PlanId id) {
        return repo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Plan> all() {
        return repo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Plan toDomain(PlanEntity e) {
        try {
            QuotaTemplate template = mapper.readValue(e.getQuotaTemplate(), QuotaTemplate.class);
            return new Plan(new PlanId(e.getPlanId()), e.getName(), template,
                    "enterprise".equals(e.getPlanId()));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to deserialize quota_template for plan "
                    + e.getPlanId(), ex);
        }
    }
}
