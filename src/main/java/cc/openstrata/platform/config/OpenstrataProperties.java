package cc.openstrata.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the {@code openstrata.*} configuration tree (SPECS §3). Profile-gated
 * feature switches and SPI provider selection live here.
 */
@Configuration
@ConfigurationProperties(prefix = "openstrata")
public class OpenstrataProperties {
    private Service service = new Service();
    private Features features = new Features();
    private Spi spi = new Spi();
    private Services services = new Services();

    public static class Services {
        private ServiceUrl toolRegistry = new ServiceUrl("http://localhost:8093");
        private ServiceUrl srs = new ServiceUrl("http://localhost:8083");
        private ServiceUrl eval = new ServiceUrl("http://localhost:8000");
        private ServiceUrl provisioning = new ServiceUrl("http://localhost:8080");

        public ServiceUrl getToolRegistry() { return toolRegistry; }
        public void setToolRegistry(ServiceUrl toolRegistry) { this.toolRegistry = toolRegistry; }
        public ServiceUrl getSrs() { return srs; }
        public void setSrs(ServiceUrl srs) { this.srs = srs; }
        public ServiceUrl getEval() { return eval; }
        public void setEval(ServiceUrl eval) { this.eval = eval; }
        public ServiceUrl getProvisioning() { return provisioning; }
        public void setProvisioning(ServiceUrl provisioning) { this.provisioning = provisioning; }
    }

    public static class ServiceUrl {
        private String url;

        public ServiceUrl() {}
        public ServiceUrl(String url) { this.url = url; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class Service {
        private int port = 8081;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Features {
        private boolean multitenancy = false;
        private boolean billing = false;
        private boolean security = false;
        private boolean riskControl = true;

        public boolean isMultitenancy() {
            return multitenancy;
        }

        public void setMultitenancy(boolean multitenancy) {
            this.multitenancy = multitenancy;
        }

        public boolean isBilling() {
            return billing;
        }

        public void setBilling(boolean billing) {
            this.billing = billing;
        }

        public boolean isSecurity() {
            return security;
        }

        public void setSecurity(boolean security) {
            this.security = security;
        }

        public boolean isRiskControl() {
            return riskControl;
        }

        public void setRiskControl(boolean riskControl) {
            this.riskControl = riskControl;
        }
    }

    public static class Spi {
        private String auth = "keycloak";
        private String multitenancy = "capsule";
        private String cache = "redis";

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        public String getMultitenancy() {
            return multitenancy;
        }

        public void setMultitenancy(String multitenancy) {
            this.multitenancy = multitenancy;
        }

        public String getCache() {
            return cache;
        }

        public void setCache(String cache) {
            this.cache = cache;
        }
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Spi getSpi() {
        return spi;
    }

    public void setSpi(Spi spi) {
        this.spi = spi;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }
}
