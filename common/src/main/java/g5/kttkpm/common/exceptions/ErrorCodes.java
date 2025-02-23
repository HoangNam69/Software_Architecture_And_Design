package g5.kttkpm.common.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCodes {
    NOT_FOUND(404, "ERROR_NOT_FOUND", "Resource not found"),
    VALIDATION_FAILED(400, "ERROR_VALIDATION", "Validation failed"),
    BUSINESS_LOGIC_FAILED(422, "ERROR_BUSINESS", "Business logic error"),
    INTERNAL_SERVER_ERROR(500, "ERROR_INTERNAL", "Internal server error"),
    UNAUTHORIZED(401, "ERROR_UNAUTHORIZED", "Unauthorized access"),
    FORBIDDEN(403, "ERROR_FORBIDDEN", "Access denied");
    
    int httpStatus;
    String code;
    String message;
}
