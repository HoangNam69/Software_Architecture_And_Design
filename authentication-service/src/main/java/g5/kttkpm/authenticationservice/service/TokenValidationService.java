package g5.kttkpm.authenticationservice.service;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenValidationService {
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Validates an access token by introspecting it with Keycloak
     *
     * @param token The access token to validate
     * @return Map containing token information if valid, or null if invalid
     */
    public Map<String, Object> validateToken(String token) {
        String introspectionUrl = issuerUri + "/protocol/openid-connect/token/introspect";
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        
        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", token);
        formData.add("token_type_hint", "access_token");
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        try {
            // Send request to Keycloak introspection endpoint
            ResponseEntity<Map> response = restTemplate.exchange(
                introspectionUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            Map<String, Object> tokenResponse = response.getBody();
            
            Map<String, Object> tokenInfo = new HashMap<>();
            if (tokenResponse != null) {
                
                // Copy relevant claims to user info
                if (tokenResponse.containsKey("exp")) tokenInfo.put("exp", tokenResponse.get("exp"));
                if (tokenResponse.containsKey("jti")) tokenInfo.put("jti", tokenResponse.get("jti"));
                if (tokenResponse.containsKey("sub")) tokenInfo.put("sub", tokenResponse.get("sub"));
                if (tokenResponse.containsKey("sid")) tokenInfo.put("sid", tokenResponse.get("sid"));
                if (tokenResponse.containsKey("email_verified")) tokenInfo.put("email_verified", tokenResponse.get("email_verified"));
                if (tokenResponse.containsKey("name")) tokenInfo.put("name", tokenResponse.get("name"));
                if (tokenResponse.containsKey("preferred_username")) tokenInfo.put("preferred_username", tokenResponse.get("preferred_username"));
                if (tokenResponse.containsKey("given_name")) tokenInfo.put("given_name", tokenResponse.get("given_name"));
                if (tokenResponse.containsKey("family_name")) tokenInfo.put("family_name", tokenResponse.get("family_name"));
                if (tokenResponse.containsKey("email")) tokenInfo.put("email", tokenResponse.get("email"));
                if (tokenResponse.containsKey("phone")) tokenInfo.put("phone", tokenResponse.get("phone"));
                if (tokenResponse.containsKey("active")) tokenInfo.put("active", tokenResponse.get("active"));
            }
            
            // Check if token is active
            if (tokenInfo != null && tokenInfo.containsKey("active") && (Boolean) tokenInfo.get("active")) {
                return tokenInfo;
            }
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error validating token: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts user information from a valid token
     *
     * @param token The access token
     * @return Map containing user profile information
     */
    public Map<String, Object> getUserInfo(String token) {
        String userInfoUrl = issuerUri + "/protocol/openid-connect/userinfo";
        
        // Prepare headers with authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        // Create request entity
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        try {
            // Send request to Keycloak userinfo endpoint
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );
            
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Error getting user info: " + e.getMessage());
            
            // If userinfo fails, fall back to token introspection data
            Map<String, Object> tokenInfo = validateToken(token);
            if (tokenInfo != null) {
                // Extract relevant user info from token data
                Map<String, Object> userInfo = new HashMap<>();
                
                // Copy relevant claims to user info
                if (tokenInfo.containsKey("sub")) userInfo.put("sub", tokenInfo.get("sub"));
                if (tokenInfo.containsKey("preferred_username")) userInfo.put("preferred_username", tokenInfo.get("preferred_username"));
                if (tokenInfo.containsKey("email")) userInfo.put("email", tokenInfo.get("email"));
                if (tokenInfo.containsKey("name")) userInfo.put("name", tokenInfo.get("name"));
                if (tokenInfo.containsKey("given_name")) userInfo.put("given_name", tokenInfo.get("given_name"));
                if (tokenInfo.containsKey("family_name")) userInfo.put("family_name", tokenInfo.get("family_name"));
                if (tokenInfo.containsKey("phone")) userInfo.put("phone", tokenInfo.get("phone"));
                
                return userInfo;
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error getting user info: " + e.getMessage());
            return null;
        }
    }
}
