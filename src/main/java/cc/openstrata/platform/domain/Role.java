package cc.openstrata.platform.domain;

public enum Role {
    PLATFORM_ADMIN("platform-admin"),
    TENANT_ADMIN("tenant-admin"),
    DEVELOPER("developer"),
    VIEWER("viewer"),
    CONSUMER("consumer");

    private final String code;

    Role(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Role fromCode(String code) {
        for (Role r : values()) {
            if (r.code.equals(code)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + code);
    }
}
