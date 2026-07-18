package cc.openstrata.platform.domain.rule;

import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.infrastructure.adapter.InMemoryPolicyRuleAdapter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRuleTest {
    private final InMemoryPolicyRuleAdapter policy = new InMemoryPolicyRuleAdapter();
    private final ApprovalRule rule = new ApprovalRule(policy);

    @Test
    void detectsHighRisk() {
        assertTrue(rule.isHighRisk("DELETE_TENANT"));
        assertTrue(rule.isHighRisk("QUOTA_INCREASE"));
    }

    @Test
    void approvedPasses() {
        policy.setApproved(true);
        rule.requireApproval("t1", "DELETE_TENANT", Map.of());
    }

    @Test
    void deniedThrows() {
        policy.setApproved(false);
        assertThrows(DomainException.class, () -> rule.requireApproval("t1", "DELETE_TENANT", Map.of()));
    }
}
