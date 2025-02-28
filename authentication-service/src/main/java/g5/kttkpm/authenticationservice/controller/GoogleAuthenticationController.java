package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.payload.GoogleRegistrationPayload;
import g5.kttkpm.authenticationservice.response.JwtResponse;
import g5.kttkpm.authenticationservice.response.RegistrationResponse;
import g5.kttkpm.authenticationservice.service.GoogleAuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthenticationController {
    
    private final GoogleAuthenticationService googleAuthenticationService;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    public GoogleAuthenticationController(GoogleAuthenticationService googleAuthenticationService) {
        this.googleAuthenticationService = googleAuthenticationService;
    }
    
    @GetMapping("/login")
    public void initiateLogin(HttpServletResponse response) throws IOException {
        String loginUrl = googleAuthenticationService.getLoginUrl();
        response.sendRedirect(loginUrl);
    }
    
    @GetMapping("/register")
    public void initiateRegistration(HttpServletResponse response) throws IOException {
        String registrationUrl = googleAuthenticationService.getRegistrationUrl();
        response.sendRedirect(registrationUrl);
    }
    
    @GetMapping("/login-url")
    public ResponseEntity<String> getLoginUrl() {
        String url = googleAuthenticationService.getLoginUrl();
        return ResponseEntity.ok(url);
    }
    
    @GetMapping("/register-url")
    public ResponseEntity<String> getRegisterUrl() {
        String url = googleAuthenticationService.getRegistrationUrl();
        return ResponseEntity.ok(url);
    }
    
    @GetMapping("/callback")
    public void handleCallback(
        @RequestParam String code,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String phone_number,
        HttpServletResponse response) throws IOException {
        
        // If it's a registration callback with phone number
        if ("register".equals(action) && phone_number != null && !phone_number.isEmpty()) {
            RegistrationResponse result = googleAuthenticationService.registerWithGoogle(code, phone_number);
            
            // Redirect to frontend with registration result
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth-callback")
                .queryParam("registration", result.status())
                .queryParam("message", result.message())
                .build().toUriString();
            
            response.sendRedirect(redirectUrl);
            return;
        }
        
        // Otherwise process as normal login
        JwtResponse tokens = googleAuthenticationService.exchangeCodeForTokens(code);
        
        // Redirect to frontend with tokens as URL parameters
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth-callback")
            .queryParam("access_token", tokens.getAccessToken())
            .queryParam("refresh_token", tokens.getRefreshToken())
            .build().toUriString();
        
        response.sendRedirect(redirectUrl);
    }
    
    /**
     * Complete Google registration with phone number
     * This is used when the frontend collects the phone number separately
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerWithGoogle(@RequestBody GoogleRegistrationPayload payload) {
        RegistrationResponse response = googleAuthenticationService.registerWithGoogle(
            payload.code(),
            payload.phoneNumber()
        );
        
        if (response.status()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
