package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.service.TokenValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * To implements this validation function, prefer to this gist:
 * <br>
 * <a href="https://gist.github.com/leminhbao308/2c7b668316423157deaee0f28c639d5c">How to implement token validation in other services</a>
 */
@RestController
@RequestMapping("/api/v1/token")
public class TokenValidationController {
    
    private final TokenValidationService tokenValidationService;
    
    public TokenValidationController(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        // Extract the token from Authorization header
        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Authorization header"));
        }
        
        // Validate the token
        Map<String, Object> tokenInfo = tokenValidationService.validateToken(token);
        
        if (tokenInfo != null) {
            return ResponseEntity.ok(tokenInfo);
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
    
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        // Extract the token from Authorization header
        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Authorization header"));
        }
        
        // First validate the token
        Map<String, Object> tokenInfo = tokenValidationService.validateToken(token);
        
        if (tokenInfo != null) {
            // If token is valid, get user info
            Map<String, Object> userInfo = tokenValidationService.getUserInfo(token);
            if (userInfo == null)
                return ResponseEntity.status(401).body(Map.of("error", "Error getting user info"));
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
