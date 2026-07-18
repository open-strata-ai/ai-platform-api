package com.openstrata.platform.application.dto;

import java.util.Map;

public record SetEntitlementsRequest(Map<String, Boolean> entitlements) {
}
