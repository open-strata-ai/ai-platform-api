package com.openstrata.platform.application.dto;

import java.util.List;

public record TenantProfileResponse(String tenantId, String name, String status, String planId,
                                    boolean multitenancyEnabled, int userCount, int appCount,
                                    List<QuotaView> quotas, List<EntitlementView> entitlements) {
}
