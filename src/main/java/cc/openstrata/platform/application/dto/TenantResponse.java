package cc.openstrata.platform.application.dto;

public record TenantResponse(String tenantId, String name, String status, String planId, boolean multitenancyEnabled) {
}
