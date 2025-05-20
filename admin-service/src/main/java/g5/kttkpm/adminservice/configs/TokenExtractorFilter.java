package g5.kttkpm.adminservice.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter để lấy token từ request đến và lưu vào TokenHolder
 */
@Component
@Order(1)  // Đảm bảo filter này chạy trước các filter khác
@RequiredArgsConstructor
@Slf4j
public class TokenExtractorFilter extends OncePerRequestFilter {
    
    private final TokenHolder tokenHolder;
    
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        
        // Lấy Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        // Kiểm tra và trích xuất token
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            tokenHolder.setToken(token);
            log.debug("JWT token extracted from request and stored in TokenHolder");
        } else {
            log.debug("No JWT token found in request headers");
        }
        
        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }
}
