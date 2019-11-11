package no.entur.uttu.error.codederror;

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.SubCode;

import java.util.Map;

public class CodedError {
    private final ErrorCode errorCode;
    private final SubCode subCode;
    private final Map<String, Object> metadata;

    public static CodedError fromErrorCode(ErrorCode errorCode) {
        return new CodedError(errorCode, null, null);
    }

    CodedError(ErrorCode errorCode, SubCode subCode) {
        this.errorCode = errorCode;
        this.subCode = subCode;
        this.metadata = Map.of();
    }

    CodedError(ErrorCode errorCode, Map<String, Object> metadata) {
        this.errorCode = errorCode;
        this.subCode = null;
        this.metadata = metadata;
    }

    CodedError(ErrorCode errorCode, SubCode subCode, Map<String, Object> metadata) {
        this.errorCode = errorCode;
        this.subCode = subCode;
        this.metadata = metadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public SubCode getSubCode() {
        return subCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
