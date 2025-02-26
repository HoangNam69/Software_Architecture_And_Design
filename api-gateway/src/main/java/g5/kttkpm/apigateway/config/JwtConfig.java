package g5.kttkpm.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

@Configuration
public class JwtConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jkwIssuer;
    
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(jkwIssuer);
    }
}
