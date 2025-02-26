package g5.kttkpm.common.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCodes errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCodes.BUSINESS_LOGIC_FAILED;
    }
    
    public BusinessException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
