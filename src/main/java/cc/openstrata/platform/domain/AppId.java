package cc.openstrata.platform.domain;

public record AppId(String value) {
    public AppId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("appId is required");
        }
    }
}
