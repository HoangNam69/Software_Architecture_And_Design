package g5.kttkpm.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {
    
    private final WebClient webClient;
    private final String tokenValidationUrl;
    
    public AuthenticationGatewayFilter(
        WebClient.Builder webClientBuilder,
        @Value("${app.auth.token-validation-url}")
        String tokenValidationUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.tokenValidationUrl = tokenValidationUrl;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for auth and token endpoints
        if (request.getURI().getPath().startsWith("/api/v1/auth")) {
            return chain.filter(exchange);
        }
        
        // Get Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // If no Authorization header, reject the request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Validate the token with auth service
        return webClient.post()
            .uri(tokenValidationUrl)
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .retrieve()
            .bodyToMono(Object.class)
            .flatMap(tokenInfo -> chain.filter(exchange))
            .onErrorResume(error -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
    }
    
    @Override
    public int getOrder() {
        // Set high priority
        return -1;
    }
}
