package g5.kttkpm.adminservice.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator/health")
@AllArgsConstructor
@Slf4j
public class ActuatorController {
    
    @GetMapping
    public ResponseEntity<?> getHealthStatus() {
        return ResponseEntity.ok("OK");
    }
}
