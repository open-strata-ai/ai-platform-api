package cc.openstrata.platform.web;

import cc.openstrata.platform.application.EntitlementAppService;
import cc.openstrata.platform.application.ModelGrantAppService;
import cc.openstrata.platform.application.dto.GrantModelRequest;
import cc.openstrata.platform.application.dto.SetEntitlementsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class EntitlementModelGrantController {
    private final EntitlementAppService entitlementAppService;
    private final ModelGrantAppService modelGrantAppService;

    public EntitlementModelGrantController(EntitlementAppService entitlementAppService,
                                            ModelGrantAppService modelGrantAppService) {
        this.entitlementAppService = entitlementAppService;
        this.modelGrantAppService = modelGrantAppService;
    }

    @PutMapping("/entitlements")
    public ResponseEntity<Void> setEntitlements(@PathVariable String tenantId,
                                                @RequestBody SetEntitlementsRequest req) {
        entitlementAppService.setEntitlements(tenantId, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/model-grants")
    public ResponseEntity<Void> grantModel(@PathVariable String tenantId, @RequestBody GrantModelRequest req) {
        modelGrantAppService.grant(tenantId, req);
        return ResponseEntity.ok().build();
    }
}
