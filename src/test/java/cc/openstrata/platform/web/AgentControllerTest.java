package cc.openstrata.platform.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.openstrata.platform.application.AgentBuildAppService;
import cc.openstrata.platform.application.AgentPublishingAppService;
import cc.openstrata.platform.application.EvalAppService;
import cc.openstrata.platform.application.ModelAuthorizationAppService;
import cc.openstrata.platform.application.SrsAppService;
import cc.openstrata.platform.application.dto.AgentResponse;
import cc.openstrata.platform.application.dto.PublishVersionRequest;
import cc.openstrata.platform.application.dto.VersionResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AgentControllerTest {
    private final AgentBuildAppService build = mock(AgentBuildAppService.class);
    private final AgentPublishingAppService publish = mock(AgentPublishingAppService.class);
    private final SrsAppService srs = mock(SrsAppService.class);
    private final EvalAppService eval = mock(EvalAppService.class);
    private final ModelAuthorizationAppService modelAuth = mock(ModelAuthorizationAppService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new AgentController(build, publish, srs, eval, modelAuth)).build();

    @BeforeEach
    void setup() {
        when(build.createAgent(any())).thenReturn(
                new AgentResponse("a1", "t1", "helper", "DRAFT", "spec"));
        when(build.getAgent(eq("a1"))).thenReturn(
                new AgentResponse("a1", "t1", "helper", "DRAFT", "spec"));
        when(publish.publishVersion(eq("a1"), any())).thenReturn(
                new VersionResponse("a1:v1", "a1", "v1", "DEPLOYED", "spec"));
        when(modelAuth.available()).thenReturn(List.of("qwen-max"));
    }

    @Test
    void createReturns201() throws Exception {
        mvc.perform(post("/api/v1/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"helper\",\"model\":\"qwen-max\",\"memoryEnabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agentId").value("a1"));
    }

    @Test
    void getReturnsAgent() throws Exception {
        mvc.perform(get("/api/v1/agents/a1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("helper"));
    }

    @Test
    void publishReturns201() throws Exception {
        mvc.perform(post("/api/v1/agents/a1/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"v1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DEPLOYED"));
    }

    @Test
    void bindSkillReturnsOk() throws Exception {
        mvc.perform(post("/api/v1/agents/a1/skills/s1:bind"))
                .andExpect(status().isOk());
    }

    @Test
    void triggerEvalReturns202() throws Exception {
        when(eval.trigger(eq("a1"), eq("ds1"))).thenReturn("run-1");
        mvc.perform(post("/api/v1/agents/a1/eval:trigger?datasetId=ds1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value("run-1"));
    }

    @Test
    void availableModelsReturnsList() throws Exception {
        mvc.perform(get("/api/v1/agents/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("qwen-max"));
    }
}
