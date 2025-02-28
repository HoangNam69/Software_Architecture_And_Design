package g5.kttkpm.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g5.kttkpm.apigateway.constant.ApiResponseConstants;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class ResponseWrapperFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseWrapperFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip wrapping for auth endpoints if needed
//        if (exchange.getRequest().getURI().getPath().startsWith("/api/v1/auth")) {
//            return chain.filter(exchange);
//        }
        
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    MediaType contentType = originalResponse.getHeaders().getContentType();
                    
                    // Only modify JSON responses
                    if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            // Collect all DataBuffers and convert to a single string
                            StringBuilder builder = new StringBuilder();
                            dataBuffers.forEach(buffer -> {
                                byte[] content = new byte[buffer.readableByteCount()];
                                buffer.read(content);
                                builder.append(new String(content, StandardCharsets.UTF_8));
                            });
                            
                            String responseBody = builder.toString();
                            
                            // Create standardized response format
                            int statusCode = originalResponse.getStatusCode() != null ?
                                originalResponse.getStatusCode().value() : ApiResponseConstants.OK;
                            
                            // Get appropriate message from constants based on status code
                            String responseMessage = ApiResponseConstants.getMessage(statusCode);
                            
                            ApiResponse<?> wrappedResponse = new ApiResponse<>(
                                statusCode,
                                responseMessage,
                                parseResponseBody(responseBody)
                            );
                            
                            try {
                                // Convert the wrapped response to JSON string
                                String wrappedBody = objectMapper.writeValueAsString(wrappedResponse);
                                byte[] bytes = wrappedBody.getBytes(StandardCharsets.UTF_8);
                                
                                // Update Content-Length header
                                originalResponse.getHeaders().setContentLength(bytes.length);
                                
                                // Create a new DataBuffer with wrapped content
                                return bufferFactory.wrap(bytes);
                            } catch (JsonProcessingException e) {
                                logger.error("Lỗi khi đóng gói phản hồi", e);
                                return bufferFactory.wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                            }
                        }).flatMap(Flux::just));
                    }
                }
                return super.writeWith(body);
            }
        };
        
        // Replace response with decorated response
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
    
    private Object parseResponseBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        
        try {
            // Try to parse the response body as JSON
            return objectMapper.readValue(body, Object.class);
        } catch (Exception e) {
            logger.warn("Không thể phân tích phản hồi dưới dạng JSON, sử dụng dưới dạng chuỗi: {}", e.getMessage());
            return body;
        }
    }
    
    @Override
    public int getOrder() {
        // Make sure this filter runs after other filters
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
    
    // Standard response format
    public record ApiResponse<T>(int code, String message, T data) {
    }
}
