package g5.kttkpm.authenticationservice.service;

import g5.kttkpm.authenticationservice.payload.RegistrationPayload;
import g5.kttkpm.authenticationservice.response.RegistrationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegistrationService {
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Registers a new user in Keycloak
     *
     * @param registrationPayload The user registration data
     * @return RegistrationResponse containing status and message
     */
    public RegistrationResponse registerUser(RegistrationPayload registrationPayload) {
        String adminTokenUrl = issuerUri.replace("/realms/tmdt", "/realms/master/protocol/openid-connect/token");
        String userRegistrationUrl = issuerUri.replace("/realms/tmdt", "/admin/realms/tmdt/users");
        
        try {
            // First, get admin token for Keycloak admin operations
            String adminToken = getAdminToken(adminTokenUrl);
            
            if (adminToken == null) {
                return new RegistrationResponse(false, "Failed to authenticate with Keycloak admin");
            }
            
            // Prepare headers with admin token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create user representation
            Map<String, Object> userData = createUserRepresentation(registrationPayload);
            
            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userData, headers);
            
            // Send request to create user
            ResponseEntity<Void> response = restTemplate.exchange(
                userRegistrationUrl,
                HttpMethod.POST,
                requestEntity,
                Void.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Set password for the user
                String userId = getUserIdByUsername(adminToken, registrationPayload.username());
                if (userId != null) {
                    if (setUserPassword(adminToken, userId, registrationPayload.password())) {
                        return new RegistrationResponse(true, "User registered successfully");
                    } else {
                        return new RegistrationResponse(false, "User created but failed to set password");
                    }
                } else {
                    return new RegistrationResponse(false, "User created but failed to retrieve user ID");
                }
            } else {
                return new RegistrationResponse(false, "Failed to register user: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new RegistrationResponse(false, "Username or email already exists");
            }
            return new RegistrationResponse(false, "Registration error: " + e.getMessage());
        } catch (Exception e) {
            return new RegistrationResponse(false, "Unexpected error during registration: " + e.getMessage());
        }
    }
    
    /**
     * Gets the admin token for Keycloak admin operations
     *
     * @param adminTokenUrl The Keycloak admin token endpoint
     * @return Admin access token
     */
    private String getAdminToken(String adminTokenUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", "admin-cli");
        formData.add("client_secret", clientSecret);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                adminTokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            Map<String, Object> tokenInfo = response.getBody();
            
            if (tokenInfo != null && tokenInfo.containsKey("access_token")) {
                return (String) tokenInfo.get("access_token");
            }
        } catch (Exception e) {
            System.err.println("Error getting admin token: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Creates a user representation from registration payload
     *
     * @param payload The registration payload
     * @return Map containing user representation for Keycloak
     */
    private Map<String, Object> createUserRepresentation(RegistrationPayload payload) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", payload.username());
        user.put("email", payload.email());
        user.put("enabled", true);
        user.put("emailVerified", false);
        
        // Add attributes for phone number
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("phone_number", Collections.singletonList(payload.phoneNumber()));
        user.put("attributes", attributes);
        
        return user;
    }
    
    /**
     * Gets a user ID by username
     *
     * @param adminToken The admin token
     * @param username The username to look up
     * @return User ID if found, null otherwise
     */
    private String getUserIdByUsername(String adminToken, String username) {
        String userSearchUrl = issuerUri.replace("/realms/tmdt", "/admin/realms/tmdt/users?username=" + username);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                userSearchUrl,
                HttpMethod.GET,
                requestEntity,
                Object[].class
            );
            
            Object[] users = response.getBody();
            
            if (users != null && users.length > 0) {
                Map<String, Object> user = (Map<String, Object>) users[0];
                return (String) user.get("id");
            }
        } catch (Exception e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Sets the password for a user
     *
     * @param adminToken The admin token
     * @param userId The user ID
     * @param password The password to set
     * @return true if successful, false otherwise
     */
    private boolean setUserPassword(String adminToken, String userId, String password) {
        String resetPasswordUrl = issuerUri.replace("/realms/tmdt", "/admin/realms/tmdt/users/" + userId + "/reset-password");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("type", "password");
        passwordData.put("value", password);
        passwordData.put("temporary", false);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(passwordData, headers);
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                resetPasswordUrl,
                HttpMethod.PUT,
                requestEntity,
                Void.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error setting user password: " + e.getMessage());
            return false;
        }
    }
}
