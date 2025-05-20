package g5.kttkpm.authenticationservice.controller;

import g5.kttkpm.authenticationservice.payload.LoginPayload;
import g5.kttkpm.authenticationservice.response.JwtResponse;
import g5.kttkpm.authenticationservice.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @RateLimiter(name = "loginRateLimiter", fallbackMethod = "loginFallback")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginPayload loginPayload) {
        JwtResponse jwtResponse = authenticationService.login(loginPayload);
        if (jwtResponse.isStatus())
            return ResponseEntity.ok(jwtResponse);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwtResponse);
    }

    public ResponseEntity<JwtResponse> loginFallback(LoginPayload loginPayload, RequestNotPermitted ex) {
        JwtResponse response = new JwtResponse();
        response.setStatus(false);
        response.setMessage("Too many login attempts. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("refresh_token") String refreshToken) {
        authenticationService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }
}
