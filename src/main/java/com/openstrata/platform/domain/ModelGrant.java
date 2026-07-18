package com.openstrata.platform.domain;

/** Per-tenant vendor model grant within the Tenant aggregate. */
public class ModelGrant {
    private final String provider;
    private final String model;

    public ModelGrant(String provider, String model) {
        this.provider = provider;
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }
}
