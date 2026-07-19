package cc.openstrata.platform.domain.port;

import java.util.List;

/** Evaluation service SPI (DV-09/DV-10/DV-17, G2). */
public interface EvalPort {
    /** Triggers an evaluation run for an agent against a dataset. Returns runId. */
    String triggerEval(String agentId, String datasetId);

    List<String> listReports(String agentId);
}
