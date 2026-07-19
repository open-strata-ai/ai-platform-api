package cc.openstrata.platform.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.QuotaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaPlanRepositoryTest {

    private PlanJpaRepository jpa;
    private JpaPlanRepository repo;

    @BeforeEach
    void setUp() {
        jpa = mock(PlanJpaRepository.class);
        repo = new JpaPlanRepository(jpa, new ObjectMapper());
    }

    @Test
    void findByIdReturnsDomainPlan() {
        PlanEntity entity = new PlanEntity();
        entity.setPlanId("trial");
        entity.setName("Trial");
        entity.setQuotaTemplate("{\"cpu\":1,\"token\":1000,\"qps\":10,\"vector\":0,\"gpu\":0}");

        when(jpa.findById("trial")).thenReturn(Optional.of(entity));

        Optional<Plan> result = repo.findById(new PlanId("trial"));
        assertTrue(result.isPresent());
        assertEquals("Trial", result.get().getName());
        assertEquals(1, result.get().getQuotaTemplate().cpu());
        assertEquals(1000, result.get().getQuotaTemplate().token());
        assertFalse(result.get().isEnterprise());
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(jpa.findById("nonexistent")).thenReturn(Optional.empty());
        assertTrue(repo.findById(new PlanId("nonexistent")).isEmpty());
    }

    @Test
    void allReturnsAllPlans() {
        PlanEntity trial = new PlanEntity();
        trial.setPlanId("trial");
        trial.setName("Trial");
        trial.setQuotaTemplate("{\"cpu\":1,\"token\":1000,\"qps\":10,\"vector\":0,\"gpu\":0}");

        PlanEntity enterprise = new PlanEntity();
        enterprise.setPlanId("enterprise");
        enterprise.setName("Enterprise");
        enterprise.setQuotaTemplate("{\"cpu\":16,\"token\":100000,\"qps\":200,\"vector\":10,\"gpu\":4}");

        when(jpa.findAll()).thenReturn(List.of(trial, enterprise));

        List<Plan> all = repo.all();
        assertEquals(2, all.size());
        assertTrue(all.get(0).isEnterprise() || all.get(1).isEnterprise());
    }

    @Test
    void throwsOnInvalidJson() {
        PlanEntity entity = new PlanEntity();
        entity.setPlanId("bad");
        entity.setName("Bad");
        entity.setQuotaTemplate("not-json");

        when(jpa.findById("bad")).thenReturn(Optional.of(entity));

        assertThrows(RuntimeException.class, () -> repo.findById(new PlanId("bad")));
    }
}
