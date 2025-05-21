package g5.kttkpm.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.List;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

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

        try {
            String rawToken = authHeader.substring(7);
            SignedJWT signedJWT = SignedJWT.parse(rawToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Map<String, Object> realmAccess = (Map<String, Object>) claims.getClaim("realm_access");
            List<String> roles = (List<String>) realmAccess.get("roles");
            boolean isAdmin = roles.contains("admin");

            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            boolean isAdminEndpoint = path.startsWith("/api/v1/admin");
            boolean isProtectedProductEndpoint = path.startsWith("/api/v1/products") &&
                    (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"));

            if ((isAdminEndpoint || isProtectedProductEndpoint) && !isAdmin) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        } catch (Exception error) {
            return Mono.defer(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
        }
    }
    
    @Override
    public int getOrder() {
        // Set high priority
        return -1;
    }
}
