package g5.kttkpm.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log request details
        logger.info("Request: {} {}", request.getMethod(), request.getURI());
        logger.debug("Headers: {}", request.getHeaders());
        
        // Start time for request
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                // Calculate duration
                long duration = System.currentTimeMillis() - startTime;
                
                // Log response details
                logger.info("Response: {} {} completed in {}ms",
                    request.getMethod(),
                    request.getURI(),
                    duration);
                
                logger.debug("Response status: {}", exchange.getResponse().getStatusCode());
            }));
    }
    
    @Override
    public int getOrder() {
        // Ensure this filter runs before the auth filter
        return -2;
    }
}
