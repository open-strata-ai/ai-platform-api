package cc.openstrata.platform.application.dto;

import java.util.List;

public record ConfigureModelRequest(String model, List<String> fallbackChain) {
}
