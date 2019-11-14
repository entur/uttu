package no.entur.uttu.error.codederror;

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.SubCode;

import java.util.HashMap;
import java.util.Map;

public class CodedError {
    private final ErrorCode errorCode;
    private final SubCode subCode;
    private final Map<String, Object> metadata;

    public static CodedError fromErrorCode(ErrorCode errorCode) {
        return new CodedError(errorCode, null, null);
    }
    public static CodedError fromErrorCode(ErrorCode errorCode, SubCode subCode) { return new CodedError(errorCode, subCode, null);}
    public static CodedError fromErrorCode(ErrorCode errorCode, SubCode subCode, Map<String, Object> metadata) { return new CodedError(errorCode, subCode, metadata);}

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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", errorCode);
        map.put("subCode", subCode);
        map.put("metadata", metadata);
        return map;
    }
}
