package g5.kttkpm.common.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<String> validationErrors;
    private final ErrorCodes errorCode;
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed");
        this.validationErrors = validationErrors;
        this.errorCode = ErrorCodes.VALIDATION_FAILED;
    }
}

