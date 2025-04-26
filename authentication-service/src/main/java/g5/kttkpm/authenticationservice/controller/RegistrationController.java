package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.payload.RegistrationPayload;
import g5.kttkpm.authenticationservice.response.RegistrationResponse;
import g5.kttkpm.authenticationservice.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationPayload registrationPayload) {
        RegistrationResponse response = registrationService.registerUser(registrationPayload);
        if (response.status()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
