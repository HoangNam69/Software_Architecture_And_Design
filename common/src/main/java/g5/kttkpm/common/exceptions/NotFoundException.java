package g5.kttkpm.common.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final ErrorCodes errorCode;
    
    public NotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCodes.NOT_FOUND;
    }
}
