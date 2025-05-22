package g5.kttkpm.paymentservice.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Lớp giữ token JWT trong phạm vi của một request.
 * Được cấu hình với @RequestScope để đảm bảo mỗi request HTTP có một instance riêng.
 */
@Getter
@Component
@RequestScope
@Slf4j
public class TokenHolder {
    
    private String token;
    
    public void setToken(String token) {
        this.token = token;
        log.debug("Token set in TokenHolder");
    }
}
