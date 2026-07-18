package cc.openstrata.platform.domain;

/** Component whitelist entry within the Tenant aggregate. */
public class Entitlement {
    private final String component;
    private boolean allowed;

    public Entitlement(String component, boolean allowed) {
        this.component = component;
        this.allowed = allowed;
    }

    public String getComponent() {
        return component;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}
