package com.openstrata.platform.web;

import com.openstrata.platform.application.ApplicationAppService;
import com.openstrata.platform.application.dto.RegisterApplicationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/applications")
public class ApplicationController {
    private final ApplicationAppService applicationAppService;

    public ApplicationController(ApplicationAppService applicationAppService) {
        this.applicationAppService = applicationAppService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@PathVariable String tenantId, @RequestBody RegisterApplicationRequest req) {
        applicationAppService.registerApplication(tenantId, req);
        return ResponseEntity.status(201).build();
    }
}
