package cc.openstrata.platform.domain.rule;

import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.QuotaTemplate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelGrantRuleTest {
    private final ModelGrantRule rule = new ModelGrantRule();
    private final Plan enterprise = new Plan(new PlanId("enterprise"), "E",
            new QuotaTemplate(1, 1, 1, 1, 1), true);
    private final Plan standard = new Plan(new PlanId("standard"), "S",
            new QuotaTemplate(1, 1, 1, 1, 1), false);

    @Test
    void enterpriseCanGrantRestricted() {
        assertDoesNotThrow(() -> rule.authorize(enterprise, "openai", "gpt-4o"));
    }

    @Test
    void standardCannotGrantRestricted() {
        assertThrows(DomainException.class, () -> rule.authorize(standard, "openai", "gpt-4o"));
    }

    @Test
    void nonRestrictedAllowedForAll() {
        assertDoesNotThrow(() -> rule.authorize(standard, "openai", "gpt-3.5"));
    }

    @Test
    void nullPlanTreatedAsNonEnterprise() {
        assertThrows(DomainException.class, () -> rule.authorize(null, "openai", "gpt-4o"));
    }
}
