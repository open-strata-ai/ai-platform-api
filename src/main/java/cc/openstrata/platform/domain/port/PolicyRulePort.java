package cc.openstrata.platform.domain.port;

import java.util.Map;

/** PolicyRule SPI (ai-srs-service). Used by ApprovalRule. */
public interface PolicyRulePort {
    boolean isApproved(String tenantId, String operation, Map<String, Object> context);
}
