package cc.openstrata.platform.application.dto;

public record CreateAgentRequest(String name, String model, boolean memoryEnabled) {
}
