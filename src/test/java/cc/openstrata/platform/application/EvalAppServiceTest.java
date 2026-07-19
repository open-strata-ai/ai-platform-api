package cc.openstrata.platform.application;

import cc.openstrata.platform.infrastructure.adapter.InMemoryEvalAdapter;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvalAppServiceTest {
    private final InMemoryEvalAdapter adapter = new InMemoryEvalAdapter();
    private final EvalAppService svc = new EvalAppService(adapter);

    @Test
    void triggerReturnsRunId() {
        String runId = svc.trigger("a1", "ds1");
        assertTrue(runId.startsWith("run-"));
        assertEquals(List.of(runId), svc.reports("a1"));
    }

    @Test
    void multipleReportsAccumulate() {
        svc.trigger("a1", "ds1");
        svc.trigger("a1", "ds2");
        assertEquals(2, svc.reports("a1").size());
    }
}
