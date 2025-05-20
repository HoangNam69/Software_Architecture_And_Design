package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.LoginPayload;
import g5.kttkpm.adminservice.dtos.RegistrationPayload;
import g5.kttkpm.adminservice.responses.JwtResponse;
import g5.kttkpm.adminservice.responses.RegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;
    
    public AuthServiceClient(WebClient.Builder webClientBuilder, @Value("${services.auth}") String authRoot) {
        this.webClient = webClientBuilder.baseUrl(authRoot).build();
        log.info("Auth service client initialized with baseUrl: {}", authRoot);
    }

    // Đăng nhập
    public Mono<JwtResponse> login(LoginPayload payload) {
        return webClient.post()
                .uri("/login")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JwtResponse.class);
    }

    // Đăng xuất
    public Mono<Void> logout(String refreshToken) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/logout")
                        .queryParam("refresh_token", refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Đăng ký
    public Mono<RegistrationResponse> register(RegistrationPayload payload) {
        return webClient.post()
                .uri("/register")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(RegistrationResponse.class);
    }

    // Xác thực token
    public Mono<Map<String, Object>> validateToken(String accessToken) {
        return webClient.post()
                .uri("/token/validate")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    // Lấy thông tin user từ token
    public Mono<Map<String, Object>> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("/token/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }
    
    public void checkStatus() {
        webClient.get()
            .uri("/cb-status")
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
