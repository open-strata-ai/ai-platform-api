package com.openstrata.platform.web;

import com.openstrata.platform.application.PlanAppService;
import com.openstrata.platform.application.QuotaAppService;
import com.openstrata.platform.application.dto.AssignPlanRequest;
import com.openstrata.platform.application.dto.UpdateQuotaRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class PlanQuotaController {
    private final PlanAppService planAppService;
    private final QuotaAppService quotaAppService;

    public PlanQuotaController(PlanAppService planAppService, QuotaAppService quotaAppService) {
        this.planAppService = planAppService;
        this.quotaAppService = quotaAppService;
    }

    @PutMapping("/plans")
    public ResponseEntity<Void> assignPlan(@PathVariable String tenantId, @RequestBody AssignPlanRequest req) {
        planAppService.assignPlan(tenantId, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/quotas")
    public ResponseEntity<Void> updateQuota(@PathVariable String tenantId, @RequestBody UpdateQuotaRequest req) {
        quotaAppService.updateQuota(tenantId, req);
        return ResponseEntity.ok().build();
    }
}
