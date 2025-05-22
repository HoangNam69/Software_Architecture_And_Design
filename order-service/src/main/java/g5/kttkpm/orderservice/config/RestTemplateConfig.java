package g5.kttkpm.orderservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestTemplateConfig {
    
    private final TokenHolder tokenHolder;
    
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        
        // Thêm interceptor để tự động thêm token vào mỗi request
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TokenRelayInterceptorForRestTemplate(tokenHolder));
        
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
    
    /**
     * Interceptor để thêm JWT token vào tất cả các request của RestTemplate
     */
    @RequiredArgsConstructor
    public static class TokenRelayInterceptorForRestTemplate implements ClientHttpRequestInterceptor {
        
        private final TokenHolder tokenHolder;
        
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            String token = tokenHolder.getToken();
            
            if (token != null && !token.isEmpty()) {
                log.debug("Adding token to request: {}", request.getURI());
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            } else {
                log.warn("No token available for request to {}", request.getURI());
            }
            
            return execution.execute(request, body);
        }
    }
}
