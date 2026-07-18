package com.openstrata.platform.domain;

/**
 * Reference to an AgentSpec: {@code apiVersion/kind/metadata.name} (DESIGN §4.3.5).
 */
public record AgentSpecRef(String apiVersion, String kind, String name) {
    public static AgentSpecRef parse(String ref) {
        if (ref == null || ref.isBlank()) {
            throw new IllegalArgumentException("agentSpecRef required");
        }
        String[] parts = ref.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("agentSpecRef must be apiVersion/kind/name");
        }
        return new AgentSpecRef(parts[0], parts[1], parts[2]);
    }

    public String toRef() {
        return apiVersion + "/" + kind + "/" + name;
    }
}
