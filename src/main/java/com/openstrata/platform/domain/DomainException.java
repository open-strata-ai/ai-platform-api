package com.openstrata.platform.domain;

/**
 * Base domain exception. Carries an {@link ErrorCode} (which implies the HTTP
 * status) so the web layer can render a uniform {@code ApiError} envelope without
 * a proliferation of exception subtypes.
 */
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
