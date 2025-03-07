package g5.kttkpm.productservice.controller;

import g5.kttkpm.productservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@CrossOrigin
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId) {
        String fileUrl = s3Service.uploadFile(file, productId);
        return ResponseEntity.ok("Upload successful. File URL: " + fileUrl);
    }

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
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