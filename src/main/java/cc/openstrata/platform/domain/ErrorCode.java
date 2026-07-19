package cc.openstrata.platform.domain;

/**
 * Business error codes for ai-platform-api. HTTP status is canonical; the
 * message is the default human-facing text (override per throw when needed).
 */
public enum ErrorCode {
    TENANT_ID_CONFLICT(409, "tenant_id already exists"),
    TENANT_NOT_FOUND(404, "Tenant aggregate does not exist"),
    TENANT_QUOTA_GPU_DISABLED(400, "GPU quota only effective when self-hosted inference (full profile modelServing) is enabled"),
    MULTITENANCY_REQUIRES_AUTH(422, "auth must be enabled before multitenancy"),
    ENTITLEMENT_DEP_VIOLATION(422, "Component whitelist violates PlatformManifest dependency graph"),
    MODEL_RESTRICTED(403, "Model requires Enterprise plan"),
    APPROVAL_REQUIRED(403, "High-risk operation requires approval"),
    QUOTA_CONFLICT(409, "Quota dimension conflict"),
    INVALID_AGENT_SPEC(400, "Agent spec is empty; build the Agent before creating a version"),
    VERSION_NOT_FOUND(404, "Agent version not found"),
    AGENT_NOT_FOUND(404, "Agent not found"),
    MODEL_NOT_AUTHORIZED(403, "Model not in tenant whitelist"),
    SRS_BINDING_CONFLICT(409, "Skill already bound to agent"),
    EVAL_DATASET_NOT_FOUND(404, "Eval dataset not found"),
    ILLEGAL_ARGUMENT(400, "Invalid argument");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String docUrl() {
        return "https://docs.openstrata.cc/errors/" + name();
    }
}
