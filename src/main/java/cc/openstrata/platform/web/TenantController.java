package cc.openstrata.platform.web;

import cc.openstrata.platform.application.TenantAppService;
import cc.openstrata.platform.application.TenantQueryService;
import cc.openstrata.platform.application.ApprovalAppService;
import cc.openstrata.platform.application.dto.CreateTenantRequest;
import cc.openstrata.platform.application.dto.PatchTenantRequest;
import cc.openstrata.platform.application.dto.SubmitApprovalRequest;
import cc.openstrata.platform.application.dto.TenantProfileResponse;
import cc.openstrata.platform.application.dto.TenantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    private final TenantAppService tenantAppService;
    private final TenantQueryService tenantQueryService;
    private final ApprovalAppService approvalAppService;

    public TenantController(TenantAppService tenantAppService, TenantQueryService tenantQueryService,
                            ApprovalAppService approvalAppService) {
        this.tenantAppService = tenantAppService;
        this.tenantQueryService = tenantQueryService;
        this.approvalAppService = approvalAppService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@RequestBody CreateTenantRequest req) {
        return ResponseEntity.status(201).body(tenantAppService.createTenant(req));
    }

    @GetMapping("/{tenantId}")
    public TenantResponse get(@PathVariable String tenantId) {
        return tenantAppService.getTenant(tenantId);
    }

    @PatchMapping("/{tenantId}")
    public TenantResponse patch(@PathVariable String tenantId, @RequestBody PatchTenantRequest req) {
        return switch (req.action()) {
            case "suspend" -> tenantAppService.suspendTenant(tenantId);
            case "resume" -> tenantAppService.resumeTenant(tenantId);
            case "delete" -> tenantAppService.deleteTenant(tenantId);
            default -> throw new IllegalArgumentException("unknown action: " + req.action());
        };
    }

    @PostMapping("/{tenantId}/multitenancy:enable")
    public TenantResponse enableMultitenancy(@PathVariable String tenantId) {
        return tenantAppService.enableMultitenancy(tenantId);
    }

    @GetMapping("/{tenantId}/profile")
    public TenantProfileResponse profile(@PathVariable String tenantId) {
        return tenantQueryService.getProfile(tenantId);
    }

    @PostMapping("/{tenantId}/approvals")
    public ResponseEntity<Void> submitApproval(@PathVariable String tenantId,
                                               @RequestBody SubmitApprovalRequest req) {
        approvalAppService.submit(tenantId, req);
        return ResponseEntity.accepted().build();
    }
}
