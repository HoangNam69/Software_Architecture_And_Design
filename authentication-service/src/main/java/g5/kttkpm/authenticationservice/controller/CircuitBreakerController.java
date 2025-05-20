package g5.kttkpm.authenticationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class CircuitBreakerController {
    
    @GetMapping("/cb-status")
    public ResponseEntity<String> getServiceStatus() {
        return ResponseEntity.ok("Auth service is running");
    }
}
