package cc.openstrata.platform.application.dto;

import java.util.List;

public record ReleaseManifestResponse(List<CapabilityView> capabilities) {
}
