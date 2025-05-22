package g5.kttkpm.paymentservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class CircuitBreakerController {
    
    @GetMapping("/cb-status")
    public void getServiceStatus() {
    }
}
