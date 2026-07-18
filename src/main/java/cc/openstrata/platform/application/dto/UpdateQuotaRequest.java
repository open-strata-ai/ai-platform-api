package cc.openstrata.platform.application.dto;

public record UpdateQuotaRequest(String dimension, long limit) {
}
