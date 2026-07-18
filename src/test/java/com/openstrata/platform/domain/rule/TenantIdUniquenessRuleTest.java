package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantIdUniquenessRuleTest {
    private final InMemoryTenantRepository repo = new InMemoryTenantRepository();
    private final TenantIdUniquenessRule rule = new TenantIdUniquenessRule(repo);

    @Test
    void rejectsDuplicate() {
        repo.save(Tenant.create(new TenantId("t1"), "Acme"));
        assertThrows(DomainException.class, () -> rule.validate(new TenantId("t1")));
    }

    @Test
    void allowsUnique() {
        assertDoesNotThrow(() -> rule.validate(new TenantId("t2")));
    }
}
