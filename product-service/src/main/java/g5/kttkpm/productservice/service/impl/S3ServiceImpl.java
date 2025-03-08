package g5.kttkpm.productservice.service.impl;

import g5.kttkpm.productservice.model.Product;
import g5.kttkpm.productservice.model.ProductImage;
import g5.kttkpm.productservice.repo.ProductImageRepository;
import g5.kttkpm.productservice.repo.ProductRepository;
import g5.kttkpm.productservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

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

            Optional<Product> productOtp = productRepository.findByProductId(productId);
            if (productOtp.isEmpty()) {
                throw new RuntimeException("Product not found");
            }

            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;

            Optional<ProductImage> productImageOpt = productImageRepository.findByProductId(productId);
            if (productImageOpt.isEmpty()) {
                productImageRepository.save(new ProductImage(null, productId, List.of(imageUrl)));
            } else {
                List<String> imageUrls = productImageOpt.get().getImageUrl();
                imageUrls.add(imageUrl);
                productImageRepository.save(new ProductImage(productImageOpt.get().getId(), productId, imageUrls));
            }
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
    public List<ProductImage> listFiles() {
        ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).build();
        ListObjectsResponse response = s3Client.listObjects(request);

        List<ProductImage> productImages = productImageRepository.findAll();

        return productImages;
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
