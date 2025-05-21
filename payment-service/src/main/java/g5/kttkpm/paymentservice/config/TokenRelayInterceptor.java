package g5.kttkpm.paymentservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Slf4j
public class TokenRelayInterceptor implements ExchangeFilterFunction {
    
    private final Supplier<String> tokenSupplier;
    
    public TokenRelayInterceptor(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // Lấy token từ supplier
        String token = tokenSupplier.get();
        
        if (token == null || token.isEmpty()) {
            // Nếu không có token, tiếp tục request mà không thêm Authorization header
            log.debug("No token available for request to {}", request.url());
            return next.exchange(request);
        }
        
        // Thêm token vào header Authorization
        log.debug("Adding Authorization token to request: {}", request.url());
        
        ClientRequest authorizedRequest = ClientRequest.from(request)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        
        return next.exchange(authorizedRequest);
    }
    
    public static ExchangeFilterFunction create(Supplier<String> tokenSupplier) {
        return new TokenRelayInterceptor(tokenSupplier);
    }
}
