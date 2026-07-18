package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.EntitlementView;
import com.openstrata.platform.application.dto.QuotaView;
import com.openstrata.platform.application.dto.TenantProfileResponse;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.port.TenantRepository;

/** Read-side projection for tenant resource profile (CQRS Query). */
public class TenantQueryService {
    private final TenantRepository tenantRepository;

    public TenantQueryService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public TenantProfileResponse getProfile(String tenantId) {
        Tenant t = tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
        var quotas = t.getQuotas().stream()
                .map(q -> new QuotaView(q.getDimension().name(), q.getLimit()))
                .toList();
        var entitlements = t.getEntitlements().stream()
                .map(e -> new EntitlementView(e.getComponent(), e.isAllowed()))
                .toList();
        return new TenantProfileResponse(t.getTenantId().value(), t.getName(), t.getStatus().name(),
                t.getPlanId() == null ? null : t.getPlanId().value(), t.isMultitenancyEnabled(),
                t.getUsers().size(), t.getApplications().size(), quotas, entitlements);
    }
}
