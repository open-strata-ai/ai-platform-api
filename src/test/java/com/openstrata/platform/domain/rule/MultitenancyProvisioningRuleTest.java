package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultitenancyProvisioningRuleTest {
    private final MultitenancyProvisioningRule rule = new MultitenancyProvisioningRule();

    @Test
    void requiresAuth() {
        assertThrows(DomainException.class, () -> rule.validate(true, false));
    }

    @Test
    void okWhenAuthEnabled() {
        assertDoesNotThrow(() -> rule.validate(true, true));
        assertDoesNotThrow(() -> rule.validate(false, false));
    }
}
