package cc.openstrata.platform.application.dto;

public record ConfigureMemoryRequest(boolean enabled, int maxTokens) {
}
