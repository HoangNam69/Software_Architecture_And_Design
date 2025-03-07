package g5.kttkpm.productservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    private String id;
    private String productId;  // Liên kết với product
    private String imageUrl;   // Đường dẫn ảnh trên S3
}