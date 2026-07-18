package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.Plan;
import com.openstrata.platform.domain.PlanId;
import com.openstrata.platform.domain.QuotaTemplate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuotaPolicyServiceTest {
    private final QuotaPolicyService service = new QuotaPolicyService();
    private final Plan enterprise = new Plan(new PlanId("enterprise"), "Enterprise",
            new QuotaTemplate(16, 100_000, 200, 10, 4), true);

    @Test
    void gpuActiveWhenModelServingEnabled() {
        QuotaTemplate policy = service.computePolicy(enterprise, true);
        assertEquals(4, policy.gpu());
    }

    @Test
    void gpuDisabledWhenModelServingOff() {
        QuotaTemplate policy = service.computePolicy(enterprise, false);
        assertEquals(0, policy.gpu());
        assertEquals(false, policy.isGpuEnabled());
    }
}
