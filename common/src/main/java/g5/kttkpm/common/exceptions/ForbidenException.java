package g5.kttkpm.common.exceptions;

import lombok.Getter;

@Getter
public class ForbidenException extends RuntimeException {
    private final ErrorCodes errorCode;
    
    public ForbidenException(String message) {
        super(message);
        this.errorCode = ErrorCodes.UNAUTHORIZED;
    }
}
