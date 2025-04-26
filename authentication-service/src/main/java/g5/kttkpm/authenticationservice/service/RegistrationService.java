package g5.kttkpm.authenticationservice.service;

import g5.kttkpm.authenticationservice.keycloak.KeycloakService;
import g5.kttkpm.authenticationservice.payload.RegistrationPayload;
import g5.kttkpm.authenticationservice.response.RegistrationResponse;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RegistrationService {
    
    private final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Autowired
    private KeycloakService keycloakService;
    
    /**
     * Registers a new user in Keycloak
     *
     * @param registrationPayload The user registration data
     * @return RegistrationResponse containing status and message
     */
    public RegistrationResponse registerUser(RegistrationPayload registrationPayload) {
        String adminTokenUrl = issuerUri.replace("/realms/tmdt", "/realms/tmdt/protocol/openid-connect/token");
        String userRegistrationUrl = issuerUri.replace("/realms/tmdt", "/admin/realms/tmdt/users");
        
        try (Keycloak keycloak = keycloakService.getKeycloakInstance()) {
            UsersResource usersResource = keycloak.realm(realm).users();
            
            // Create password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(registrationPayload.password());
            credential.setTemporary(false);
            
            // Set user info
            UserRepresentation user = new UserRepresentation();
            user.setUsername(registrationPayload.username());
            user.setFirstName(registrationPayload.firstName());
            user.setLastName(registrationPayload.lastName());
            user.setEmail(registrationPayload.email());
            user.setCredentials(Collections.singletonList(credential));
            user.setAttributes(Collections.singletonMap("phone", Collections.singletonList(registrationPayload.phone())));
            user.setEnabled(true);
            
            // Create user
            Response response = usersResource.create(user);
            
            if (response.getStatus() == HttpStatus.CREATED.value()) {
                return new RegistrationResponse(true, "User registered successfully", user);
            }
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                return new RegistrationResponse(false, "Failed to create user", "username or email has been used");
            } else {
                logger.error(response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase() + ": " + "Failed to create user");
                return new RegistrationResponse(false, "Failed to create user", null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            return new RegistrationResponse(false, "An error has occured, please try again", null);
        }
    }
    
}
