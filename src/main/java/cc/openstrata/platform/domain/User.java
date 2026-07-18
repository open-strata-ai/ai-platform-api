package cc.openstrata.platform.domain;

/** User entity within the Tenant aggregate. */
public class User {
    private final UserId userId;
    private final TenantId tenantId;
    private String email;
    private Role role;
    private UserStatus status;
    private String kcId;

    public User(UserId userId, TenantId tenantId, String email, Role role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.role = role;
        this.status = UserStatus.INVITED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public UserId getUserId() {
        return userId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getKcId() {
        return kcId;
    }

    public void setKcId(String kcId) {
        this.kcId = kcId;
    }
}
