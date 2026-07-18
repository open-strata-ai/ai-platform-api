package cc.openstrata.platform.domain.rule;

import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.port.TenantRepository;

/** RULE-01: tenant_id MUST be globally unique (DB constraint + domain pre-check). */
public class TenantIdUniquenessRule {
    private final TenantRepository tenantRepository;

    public TenantIdUniquenessRule(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void validate(TenantId id) {
        if (tenantRepository.exists(id)) {
            throw new DomainException(ErrorCode.TENANT_ID_CONFLICT,
                    "tenant_id " + id.value() + " already exists");
        }
    }
}
