package g5.kttkpm.productservice.service;

import g5.kttkpm.productservice.model.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    String uploadFile(MultipartFile file, String productId);
    List<ProductImage> listFiles();
    String getFileUrl(String fileName);
    void deleteFile(String fileName);
}