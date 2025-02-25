package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.response.JwtResponse;
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
    
    @GetMapping("/login-url")
    public ResponseEntity<String> getLoginUrl() {
        String url = googleAuthenticationService.getLoginUrl();
        return ResponseEntity.ok(url);
    }
    
    @GetMapping("/callback")
    public void handleCallback(
        @RequestParam String code,
        HttpServletResponse response) throws IOException {
        
        JwtResponse tokens = googleAuthenticationService.exchangeCodeForTokens(code);
        
        // Redirect to frontend with tokens as URL parameters
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth-callback")
            .queryParam("access_token", tokens.getAccessToken())
            .queryParam("refresh_token", tokens.getRefreshToken())
            .build().toUriString();
        
        response.sendRedirect(redirectUrl);
    }
}
