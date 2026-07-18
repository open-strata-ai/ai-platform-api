package cc.openstrata.platform.web;

/**
 * Uniform error envelope returned by the API (SPECS §1.4).
 */
public class ApiError {
    private String code;
    private String message;
    private String traceId;
    private String doc;

    public static ApiError of(String code, String message, String traceId) {
        ApiError e = new ApiError();
        e.code = code;
        e.message = message;
        e.traceId = traceId;
        e.doc = "https://docs.openstrata.cc/errors/" + code;
        return e;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getDoc() {
        return doc;
    }
}
