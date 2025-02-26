package g5.kttkpm.common.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final ErrorCodes errorCode;
    
    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = ErrorCodes.UNAUTHORIZED;
    }
}

