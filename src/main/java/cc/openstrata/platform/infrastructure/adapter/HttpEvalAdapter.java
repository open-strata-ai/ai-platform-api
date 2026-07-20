package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.EvalPort;
import cc.openstrata.platform.infrastructure.persistence.EvalRunEntity;
import cc.openstrata.platform.infrastructure.persistence.EvalRunJpaRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClient;

/**
 * Production {@link EvalPort}: triggers an eval run in ai-eval-service over HTTP
 * and records the returned run id in platform-api's own DB so reports can be
 * listed per agent (the eval service has no agent-indexed report listing).
 */
public class HttpEvalAdapter implements EvalPort {

    private final RestClient client;
    private final EvalRunJpaRepository runRepo;

    public HttpEvalAdapter(RestClient.Builder builder, EvalRunJpaRepository runRepo) {
        this.client = builder.build();
        this.runRepo = runRepo;
    }

    @Override
    public String triggerEval(String agentId, String datasetId) {
        Map<String, Object> body = Map.of(
                "dataset_id", datasetId,
                "dataset_version", "v1",
                "agent", Map.of("agent_id", agentId, "tenant_id", "local"),
                "scorer_set", List.of("accuracy"),
                "split", "eval");
        Map<String, Object> resp = client.post()
                .uri("/v1/runs")
                .body(body)
                .retrieve()
                .body(Map.class);
        String runId = resp == null ? null : String.valueOf(resp.get("run_id"));
        if (runId != null) {
            EvalRunEntity e = new EvalRunEntity();
            e.setAgentId(agentId);
            e.setRunId(runId);
            runRepo.save(e);
        }
        return runId;
    }

    @Override
    public List<String> listReports(String agentId) {
        return runRepo.findByAgentId(agentId).stream()
                .map(EvalRunEntity::getRunId)
                .toList();
    }
}
