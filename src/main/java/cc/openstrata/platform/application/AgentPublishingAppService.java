package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.PublishVersionRequest;
import cc.openstrata.platform.application.dto.VersionResponse;
import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.AgentVersionService;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.domain.port.DeploymentPort;
import java.util.ArrayList;
import java.util.List;

/** Agent publishing + version lifecycle (DV-11, DV-15). */
public class AgentPublishingAppService {
    private final AgentRepository agentRepository;
    private final AgentVersionService versionService;
    private final DeploymentPort deploymentPort;

    public AgentPublishingAppService(AgentRepository agentRepository,
                                     AgentVersionService versionService,
                                     DeploymentPort deploymentPort) {
        this.agentRepository = agentRepository;
        this.versionService = versionService;
        this.deploymentPort = deploymentPort;
    }

    private Agent load(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new DomainException(ErrorCode.AGENT_NOT_FOUND));
    }

    /** Publish a version: snapshot spec, mark DEPLOYED via DeploymentPort. */
    public VersionResponse publishVersion(String agentId, PublishVersionRequest req) {
        Agent agent = load(agentId);
        String version = req.version() != null && !req.version().isBlank() ? req.version() : "v1";
        AgentVersion v = versionService.createVersion(agent, version);
        deploymentPort.deploy(agent.getTenantId(), agentId, version, v.getSpecSnapshot());
        agentRepository.updateVersionStatus(v.getVersionId(), AgentVersion.STATUS_DEPLOYED);
        agent.publish();
        agentRepository.save(agent);
        return new VersionResponse(v.getVersionId(), agentId, v.getVersion(),
                AgentVersion.STATUS_DEPLOYED, v.getSpecSnapshot());
    }

    /** Rollback to a previously published version. */
    public VersionResponse rollbackToVersion(String agentId, String version) {
        Agent agent = load(agentId);
        AgentVersion target = agentRepository.findVersion(agentId, version)
                .orElseThrow(() -> new DomainException(ErrorCode.VERSION_NOT_FOUND));
        versionService.validateRollback(target);
        deploymentPort.rollback(agentId, version);
        agentRepository.updateVersionStatus(target.getVersionId(), AgentVersion.STATUS_ROLLED_BACK);
        return new VersionResponse(target.getVersionId(), agentId, target.getVersion(),
                AgentVersion.STATUS_ROLLED_BACK, target.getSpecSnapshot());
    }

    public VersionResponse getVersion(String agentId, String version) {
        AgentVersion v = agentRepository.findVersion(agentId, version)
                .orElseThrow(() -> new DomainException(ErrorCode.VERSION_NOT_FOUND));
        return new VersionResponse(v.getVersionId(), agentId, v.getVersion(), v.getStatus(), v.getSpecSnapshot());
    }

    public List<VersionResponse> listVersions(String agentId) {
        List<VersionResponse> out = new ArrayList<>();
        for (AgentVersion v : agentRepository.findVersions(agentId)) {
            out.add(new VersionResponse(v.getVersionId(), agentId, v.getVersion(), v.getStatus(), v.getSpecSnapshot()));
        }
        return out;
    }
}
