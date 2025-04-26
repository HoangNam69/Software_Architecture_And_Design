package g5.kttkpm.paymentservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OrderData {
    
    @Positive
    private Integer amount;                 // Số tiền thanh toán
    
    @NotNull
    private String description;             // Mô tả cho thanh toán, được dùng làm nội dung chuyển khoản
    
    @NotEmpty
    private List<ProductData> products;           // 	Danh sách các sản phẩm
    
    @NotEmpty
    private String cancelUrl;               // Đường dẫn sẽ được chuyển tiếp về trang web khi người dùng bấm hủy đơn hàng
    
    @NotEmpty
    private String returnUrl;               // Đường dẫn sẽ được chuyển tiếp về trang web khi người dùng đã thanh toán đơn hàng thành công
    
    private String buyerName;               // Tên của người mua hàng.
    
    private String buyerEmail;              // Email của người mua hàng.
    
    private String buyerPhone;              // Số điện thoại người mua hàng.
}
