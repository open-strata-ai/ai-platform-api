package com.openstrata.platform.web;

import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest req) {
        ApiError body = ApiError.of(ex.getErrorCode().name(), ex.getMessage(), traceId());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex, HttpServletRequest req) {
        ApiError body = ApiError.of(ErrorCode.ILLEGAL_ARGUMENT.name(), ex.getMessage(), traceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        ApiError body = ApiError.of("INTERNAL_ERROR", ex.getMessage(), traceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String traceId() {
        String id = MDC.get("traceId");
        return id != null ? id : "n/a";
    }
}
