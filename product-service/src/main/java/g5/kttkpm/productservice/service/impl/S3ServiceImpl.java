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

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String productId) {
        String fileName = productId + "_" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
            );

            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            productImageRepository.save(new ProductImage(null, productId, imageUrl));

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
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