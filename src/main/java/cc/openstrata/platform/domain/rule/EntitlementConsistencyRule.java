package cc.openstrata.platform.domain.rule;

import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import java.util.Set;

/**
 * RULE-04: component whitelist must satisfy the PlatformManifest dependency
 * graph. billing -> multitenancy -> auth.
 */
public class EntitlementConsistencyRule {
    public void validate(Set<String> entitlements) {
        boolean billing = entitlements.contains("billing");
        boolean multitenancy = entitlements.contains("multitenancy");
        boolean auth = entitlements.contains("auth");
        if (billing && !multitenancy) {
            throw new DomainException(ErrorCode.ENTITLEMENT_DEP_VIOLATION,
                    "billing requires multitenancy");
        }
        if (multitenancy && !auth) {
            throw new DomainException(ErrorCode.ENTITLEMENT_DEP_VIOLATION,
                    "multitenancy requires auth");
        }
    }
}
