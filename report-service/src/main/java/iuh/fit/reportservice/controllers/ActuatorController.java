package iuh.fit.reportservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator/health")
public class ActuatorController {
    
    @GetMapping
    public ResponseEntity<?> getHealthStatus() {
        return ResponseEntity.ok("OK");
    }
}
