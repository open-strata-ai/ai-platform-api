package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntitlementConsistencyRuleTest {
    private final EntitlementConsistencyRule rule = new EntitlementConsistencyRule();

    @Test
    void billingRequiresMultitenancy() {
        assertThrows(DomainException.class, () -> rule.validate(Set.of("billing")));
    }

    @Test
    void multitenancyRequiresAuth() {
        assertThrows(DomainException.class, () -> rule.validate(Set.of("multitenancy")));
    }

    @Test
    void validFullChain() {
        assertDoesNotThrow(() -> rule.validate(Set.of("auth", "multitenancy", "billing")));
        assertDoesNotThrow(() -> rule.validate(Set.of("auth", "multitenancy")));
    }
}
