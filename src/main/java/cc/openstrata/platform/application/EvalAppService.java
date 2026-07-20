package cc.openstrata.platform.application;

import cc.openstrata.platform.domain.port.EvalPort;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: trigger and list evaluations for an Agent (DV-09, G2). */
@Service
public class EvalAppService {
    private final EvalPort eval;

    public EvalAppService(EvalPort eval) {
        this.eval = eval;
    }

    public String trigger(String agentId, String datasetId) {
        return eval.triggerEval(agentId, datasetId);
    }

    public List<String> reports(String agentId) {
        return eval.listReports(agentId);
    }
}
