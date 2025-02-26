package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.payload.LoginPayload;
import g5.kttkpm.authenticationservice.response.JwtResponse;
import g5.kttkpm.authenticationservice.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginPayload loginPayload) {
        JwtResponse jwtResponse = authenticationService.login(loginPayload);
        return ResponseEntity.ok(jwtResponse);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("refresh_token") String refreshToken) {
        authenticationService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }
}
