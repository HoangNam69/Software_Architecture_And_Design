package g5.kttkpm.common.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(ErrorCodes.NOT_FOUND.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.NOT_FOUND, ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(ValidationException ex) {
        String errors = String.join(", ", ex.getValidationErrors());
        return ResponseEntity.status(ErrorCodes.VALIDATION_FAILED.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.VALIDATION_FAILED, ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ErrorCodes.BUSINESS_LOGIC_FAILED.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.BUSINESS_LOGIC_FAILED, ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        return ResponseEntity.status(ErrorCodes.VALIDATION_FAILED.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.VALIDATION_FAILED, ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        return ResponseEntity.status(ErrorCodes.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_SERVER_ERROR.getMessage()));
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(ErrorCodes.UNAUTHORIZED.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED.getMessage()));
    }
    
    @ExceptionHandler(ForbidenException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidenException(ForbidenException ex) {
        return ResponseEntity.status(ErrorCodes.FORBIDDEN.getHttpStatus())
            .body(new ErrorResponseDTO(ErrorCodes.FORBIDDEN, ErrorCodes.FORBIDDEN.getMessage()));
    }
}
