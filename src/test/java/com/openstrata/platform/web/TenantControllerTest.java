package com.openstrata.platform.web;

import com.openstrata.platform.application.ApprovalAppService;
import com.openstrata.platform.application.TenantAppService;
import com.openstrata.platform.application.TenantQueryService;
import com.openstrata.platform.application.dto.CreateTenantRequest;
import com.openstrata.platform.application.dto.TenantResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantControllerTest {
    private final TenantAppService tenantSvc = mock(TenantAppService.class);
    private final TenantQueryService querySvc = mock(TenantQueryService.class);
    private final ApprovalAppService approvalSvc = mock(ApprovalAppService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new TenantController(tenantSvc, querySvc, approvalSvc)).build();

    @Test
    void createReturns201WithBody() throws Exception {
        when(tenantSvc.createTenant(any())).thenReturn(
                new TenantResponse("t1", "Acme", "PROVISIONING", null, false));
        mvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("t1"))
                .andExpect(jsonPath("$.name").value("Acme"));
    }
}
