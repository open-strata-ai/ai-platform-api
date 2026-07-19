package cc.openstrata.platform.application.dto;

public record VersionResponse(String versionId, String agentId, String version, String status, String specSnapshot) {
}
