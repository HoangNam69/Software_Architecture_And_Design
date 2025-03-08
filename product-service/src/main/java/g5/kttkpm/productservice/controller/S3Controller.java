package g5.kttkpm.productservice.controller;

import g5.kttkpm.productservice.model.ProductImage;
import g5.kttkpm.productservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products/files")
@RequiredArgsConstructor
@CrossOrigin
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId) {
        String fileUrl = s3Service.uploadFile(file, productId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "File created");
        response.put("url", URI.create(fileUrl));
        
        return ResponseEntity.created(URI.create(fileUrl)).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductImage>> listFiles() {
        return ResponseEntity.ok(s3Service.listFiles());
    }

    @GetMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam("fileName") String fileName) {
        String url = s3Service.getFileUrl(fileName);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully.");
    }
}
