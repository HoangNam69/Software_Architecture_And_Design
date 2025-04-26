package g5.kttkpm.authenticationservice.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    
    @Value("${keycloak.admin-realm}")
    private String adminRealm;
    
    @Value("${keycloak.admin-config.username}")
    private String username;
    
    @Value("${keycloak.admin-config.password}")
    private String password;
    
    public Keycloak getKeycloakInstance() {
        return Keycloak.getInstance(
            authServerUrl,
            adminRealm,
            username,
            password,
            "admin-cli"
        );
    }
    
}
