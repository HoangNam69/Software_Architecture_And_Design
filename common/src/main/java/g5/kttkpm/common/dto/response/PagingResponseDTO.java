package g5.kttkpm.common.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;


/**
 * Dùng cho API trả về kết quả có phân trang.
 *
 * @param <T> Class data tương ứng
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagingResponseDTO<T> extends ApiResponseDTO<List<T>> {
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPages;
    
    public PagingResponseDTO(boolean success, String message, List<T> data, int pageNumber, int pageSize, long totalElements, int totalPages) {
        super(success, message, data);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
