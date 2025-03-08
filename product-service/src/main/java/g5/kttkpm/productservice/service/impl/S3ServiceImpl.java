package g5.kttkpm.productservice.service.impl;

import g5.kttkpm.productservice.model.ProductImage;
import g5.kttkpm.productservice.repo.ProductImageRepository;
import g5.kttkpm.productservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final ProductImageRepository productImageRepository;

    @Value("${aws.bucket-name}")
    private String bucketName;
    
    @Override
    public String uploadFile(MultipartFile file, String productId) {
        String fileName = productId + "_" + file.getOriginalFilename();
        
        try {
            // Xác định Content-Type dựa trên file
            String contentType = file.getContentType();
            if (contentType == null) {
                // Nếu không xác định được từ MultipartFile, thử xác định từ tên file
                contentType = determineContentType(file.getOriginalFilename());
            }
            
            // Tạo request với content type và ACL public-read
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ) // Thêm ACL để cho phép public access
                .build();
            
            s3Client.putObject(
                request,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
            );
            
            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            productImageRepository.save(new ProductImage(null, productId, imageUrl));
            
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }
    
    // Phương thức hỗ trợ xác định content type từ tên file
    private String determineContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        fileName = fileName.toLowerCase();
        
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "application/msword";
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".mp3")) {
            return "audio/mpeg";
        }
        
        // Default binary stream
        return "application/octet-stream";
    }

    @Override
    public List<String> listFiles() {
        ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).build();
        ListObjectsResponse response = s3Client.listObjects(request);

        return response.contents().stream()
                .map(s3Object -> getFileUrl(s3Object.key()))
                .collect(Collectors.toList());
    }

    @Override
    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());

        productImageRepository.deleteById(fileName);
    }
}
