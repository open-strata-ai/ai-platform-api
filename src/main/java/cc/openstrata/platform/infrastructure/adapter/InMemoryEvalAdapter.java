package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.EvalPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** In-memory EvalPort stand-in for Batch G2 tests (DV-09, DV-17). */
public class InMemoryEvalAdapter implements EvalPort {
    private final Map<String, List<String>> reports = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public String triggerEval(String agentId, String datasetId) {
        String runId = "run-" + seq.incrementAndGet();
        reports.computeIfAbsent(agentId, k -> new ArrayList<>()).add(runId);
        return runId;
    }

    @Override
    public List<String> listReports(String agentId) {
        return new ArrayList<>(reports.getOrDefault(agentId, List.of()));
    }
}
