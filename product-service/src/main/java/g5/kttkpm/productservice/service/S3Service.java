package g5.kttkpm.productservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    String uploadFile(MultipartFile file, String productId);
    List<String> listFiles();
    String getFileUrl(String fileName);
    void deleteFile(String fileName);
}