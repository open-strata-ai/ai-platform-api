package cc.openstrata.platform.web;

import cc.openstrata.platform.application.dto.CapabilityView;
import cc.openstrata.platform.application.dto.ReleaseManifestResponse;
import cc.openstrata.platform.config.OpenstrataProperties;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only capability manifest (SPECS §1.2). */
@RestController
@RequestMapping("/release")
public class ReleaseController {
    private final OpenstrataProperties properties;

    public ReleaseController(OpenstrataProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/manifest")
    public ReleaseManifestResponse manifest() {
        var f = properties.getFeatures();
        String auth = properties.getSpi().getAuth();
        boolean authEnabled = auth != null && !"none".equals(auth);
        List<CapabilityView> caps = List.of(
                new CapabilityView("multitenancy", f.isMultitenancy()),
                new CapabilityView("billing", f.isBilling()),
                new CapabilityView("security", f.isSecurity()),
                new CapabilityView("riskControl", f.isRiskControl()),
                new CapabilityView("auth", authEnabled));
        return new ReleaseManifestResponse(caps);
    }
}
