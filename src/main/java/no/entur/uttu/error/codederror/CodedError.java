package no.entur.uttu.error.codederror;

import no.entur.uttu.error.ErrorCodeEnumeration;

import java.util.Map;

public class CodedError {
    private final ErrorCodeEnumeration errorCode;
    private final Map<String, Object> metadata;

    public static CodedError fromErrorCode(ErrorCodeEnumeration errorCode) {
        return new CodedError(errorCode, null);
    }

    CodedError(ErrorCodeEnumeration errorCode, Map<String, Object> metadata) {
        this.errorCode = errorCode;
        this.metadata = metadata;
    }

    public ErrorCodeEnumeration getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
