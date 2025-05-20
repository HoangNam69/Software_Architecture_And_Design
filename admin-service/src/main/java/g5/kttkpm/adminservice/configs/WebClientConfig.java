package g5.kttkpm.adminservice.configs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Cấu hình WebClient để thêm token vào tất cả request
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {
    
    private final TokenHolder tokenHolder;
    
    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .filter(TokenRelayInterceptor.create(tokenHolder::getToken))
            .filter((request, next) -> {
                log.debug("Sending request to: {}", request.url());
                return next.exchange(request);
            });
    }
}
