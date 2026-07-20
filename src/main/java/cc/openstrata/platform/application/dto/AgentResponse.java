package cc.openstrata.platform.application.dto;

public record AgentResponse(String agentId, String tenantId, String name, String status, String spec) {
}
