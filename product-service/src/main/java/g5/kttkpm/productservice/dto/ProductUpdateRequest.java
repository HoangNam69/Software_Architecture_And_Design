package g5.kttkpm.productservice.dto;

import g5.kttkpm.productservice.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductUpdateRequest {
    // Phần thông tin cơ bản
    private String name;
    private String sku;
    private String description;
    private String brand;
    private String thumbnailUrl;
    private List<String> imageUrls;
    
    // Phần danh mục
    private String mainCategoryId;
    private List<String> additionalCategories;
    
    // Phần giá
    private BigDecimal currentPrice;
    private Product.PriceChangeReason priceChangeReason;
    private String priceChangedBy;
    
    // Phần số lượng
    private Integer totalQuantity;
    private Product.QuantityChangeReason quantityChangeReason;
    private String quantityChangedBy;
    
    // Phần trạng thái
    private Product.ProductStatus status;
    
    // Phần thuộc tính bổ sung
    private Map<String, Object> additionalAttributes;
    
    // Phần điều khiển cập nhật
    private UpdateOperationType operation;
    
    // Định nghĩa các loại cập nhật có sẵn
    public enum UpdateOperationType {
        ALL,                // Cập nhật tất cả các trường được cung cấp
        BASIC_INFO,         // Chỉ cập nhật thông tin cơ bản
        PRICE,              // Chỉ cập nhật giá
        QUANTITY,           // Chỉ cập nhật số lượng
        STATUS,             // Chỉ cập nhật trạng thái
        CATEGORIES,         // Chỉ cập nhật danh mục
        ATTRIBUTES          // Chỉ cập nhật thuộc tính bổ sung
    }
}
