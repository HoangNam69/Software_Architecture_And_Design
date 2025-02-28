package g5.kttkpm.apigateway.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants class for HTTP status codes and Vietnamese response messages
 */
public class ApiResponseConstants {
    
    // Success codes (2xx)
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NO_CONTENT = 204;
    
    // Client error codes (4xx)
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int PRECONDITION_FAILED = 412;
    public static final int PAYLOAD_TOO_LARGE = 413;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int TOO_MANY_REQUESTS = 429;
    
    // Server error codes (5xx)
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    
    // Business logic error codes (custom)
    public static final int VALIDATION_ERROR = 1000;
    public static final int BUSINESS_ERROR = 1001;
    public static final int DATA_INTEGRITY_ERROR = 1002;
    
    // Map of HTTP status codes to Vietnamese messages
    private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
    
    static {
        // Success messages
        STATUS_MESSAGES.put(OK, "Thành công");
        STATUS_MESSAGES.put(CREATED, "Đã tạo thành công");
        STATUS_MESSAGES.put(ACCEPTED, "Đã tiếp nhận yêu cầu");
        STATUS_MESSAGES.put(NO_CONTENT, "Không có nội dung");
        
        // Client error messages
        STATUS_MESSAGES.put(BAD_REQUEST, "Yêu cầu không hợp lệ");
        STATUS_MESSAGES.put(UNAUTHORIZED, "Chưa xác thực");
        STATUS_MESSAGES.put(FORBIDDEN, "Không có quyền truy cập");
        STATUS_MESSAGES.put(NOT_FOUND, "Không tìm thấy");
        STATUS_MESSAGES.put(METHOD_NOT_ALLOWED, "Phương thức không được phép");
        STATUS_MESSAGES.put(CONFLICT, "Xung đột dữ liệu");
        STATUS_MESSAGES.put(GONE, "Tài nguyên không còn tồn tại");
        STATUS_MESSAGES.put(PRECONDITION_FAILED, "Điều kiện tiên quyết thất bại");
        STATUS_MESSAGES.put(PAYLOAD_TOO_LARGE, "Dữ liệu quá lớn");
        STATUS_MESSAGES.put(UNSUPPORTED_MEDIA_TYPE, "Định dạng không được hỗ trợ");
        STATUS_MESSAGES.put(TOO_MANY_REQUESTS, "Quá nhiều yêu cầu");
        
        // Server error messages
        STATUS_MESSAGES.put(INTERNAL_SERVER_ERROR, "Lỗi hệ thống");
        STATUS_MESSAGES.put(NOT_IMPLEMENTED, "Chức năng chưa được xây dựng");
        STATUS_MESSAGES.put(BAD_GATEWAY, "Lỗi cổng kết nối");
        STATUS_MESSAGES.put(SERVICE_UNAVAILABLE, "Dịch vụ không khả dụng");
        STATUS_MESSAGES.put(GATEWAY_TIMEOUT, "Cổng kết nối hết thời gian chờ");
        
        // Business logic error messages
        STATUS_MESSAGES.put(VALIDATION_ERROR, "Lỗi xác thực dữ liệu");
        STATUS_MESSAGES.put(BUSINESS_ERROR, "Lỗi nghiệp vụ");
        STATUS_MESSAGES.put(DATA_INTEGRITY_ERROR, "Lỗi toàn vẹn dữ liệu");
    }
    
    /**
     * Get the Vietnamese message for a specific HTTP status code
     *
     * @param statusCode The HTTP status code
     * @return The corresponding Vietnamese message or a default message if not found
     */
    public static String getMessage(int statusCode) {
        return STATUS_MESSAGES.getOrDefault(statusCode, "Không xác định");
    }
    
    /**
     * Check if status code represents a success (2xx)
     *
     * @param statusCode The HTTP status code
     * @return true if the status code is in the 2xx range
     */
    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Check if status code represents a client error (4xx)
     *
     * @param statusCode The HTTP status code
     * @return true if the status code is in the 4xx range
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Check if status code represents a server error (5xx)
     *
     * @param statusCode The HTTP status code
     * @return true if the status code is in the 5xx range
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * Check if status code represents a business logic error (custom codes)
     *
     * @param statusCode The HTTP status code
     * @return true if the status code is in the custom business error range
     */
    public static boolean isBusinessError(int statusCode) {
        return statusCode >= 1000 && statusCode < 2000;
    }
}
