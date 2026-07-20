package cc.openstrata.platform.web;

import cc.openstrata.platform.application.AgentBuildAppService;
import cc.openstrata.platform.application.AgentPublishingAppService;
import cc.openstrata.platform.application.EvalAppService;
import cc.openstrata.platform.application.ModelAuthorizationAppService;
import cc.openstrata.platform.application.SrsAppService;
import cc.openstrata.platform.application.dto.AgentResponse;
import cc.openstrata.platform.application.dto.BindToolRequest;
import cc.openstrata.platform.application.dto.ConfigureMemoryRequest;
import cc.openstrata.platform.application.dto.ConfigureModelRequest;
import cc.openstrata.platform.application.dto.CreateAgentRequest;
import cc.openstrata.platform.application.dto.PublishVersionRequest;
import cc.openstrata.platform.application.dto.VersionResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    private final AgentBuildAppService buildService;
    private final AgentPublishingAppService publishingService;
    private final SrsAppService srsAppService;
    private final EvalAppService evalAppService;
    private final ModelAuthorizationAppService modelAuthService;

    public AgentController(AgentBuildAppService buildService, AgentPublishingAppService publishingService,
                           SrsAppService srsAppService, EvalAppService evalAppService,
                           ModelAuthorizationAppService modelAuthService) {
        this.buildService = buildService;
        this.publishingService = publishingService;
        this.srsAppService = srsAppService;
        this.evalAppService = evalAppService;
        this.modelAuthService = modelAuthService;
    }

    @PostMapping
    public ResponseEntity<AgentResponse> create(@RequestBody CreateAgentRequest req) {
        return ResponseEntity.status(201).body(buildService.createAgent(req));
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentResponse> get(@PathVariable String agentId) {
        return ResponseEntity.ok(buildService.getAgent(agentId));
    }

    @PostMapping("/{agentId}/tools")
    public ResponseEntity<AgentResponse> bindTool(@PathVariable String agentId, @RequestBody BindToolRequest req) {
        return ResponseEntity.ok(buildService.bindTool(agentId, req));
    }

    @PostMapping("/{agentId}/memory")
    public ResponseEntity<AgentResponse> configureMemory(@PathVariable String agentId, @RequestBody ConfigureMemoryRequest req) {
        return ResponseEntity.ok(buildService.configureMemory(agentId, req));
    }

    @PostMapping("/{agentId}/model")
    public ResponseEntity<AgentResponse> configureModel(@PathVariable String agentId, @RequestBody ConfigureModelRequest req) {
        return ResponseEntity.ok(buildService.configureModel(agentId, req));
    }

    @PostMapping("/{agentId}/versions")
    public ResponseEntity<VersionResponse> publish(@PathVariable String agentId, @RequestBody PublishVersionRequest req) {
        return ResponseEntity.status(201).body(publishingService.publishVersion(agentId, req));
    }

    @GetMapping("/{agentId}/versions")
    public ResponseEntity<List<VersionResponse>> versions(@PathVariable String agentId) {
        return ResponseEntity.ok(publishingService.listVersions(agentId));
    }

    @PostMapping("/{agentId}/versions/{version}:rollback")
    public ResponseEntity<VersionResponse> rollback(@PathVariable String agentId, @PathVariable String version) {
        return ResponseEntity.ok(publishingService.rollbackToVersion(agentId, version));
    }

    @PostMapping("/{agentId}/skills/{skillId}:bind")
    public ResponseEntity<Void> bindSkill(@PathVariable String agentId, @PathVariable String skillId) {
        srsAppService.bindSkill(agentId, skillId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{agentId}/skills")
    public ResponseEntity<List<String>> boundSkills(@PathVariable String agentId) {
        return ResponseEntity.ok(srsAppService.boundSkills(agentId));
    }

    @PostMapping("/{agentId}/eval:trigger")
    public ResponseEntity<Map<String, String>> triggerEval(@PathVariable String agentId,
                                                           @RequestParam String datasetId) {
        String runId = evalAppService.trigger(agentId, datasetId);
        return ResponseEntity.status(202).body(Map.of("runId", runId));
    }

    @GetMapping("/{agentId}/eval")
    public ResponseEntity<List<String>> evalReports(@PathVariable String agentId) {
        return ResponseEntity.ok(evalAppService.reports(agentId));
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> availableModels() {
        return ResponseEntity.ok(modelAuthService.available());
    }
}
