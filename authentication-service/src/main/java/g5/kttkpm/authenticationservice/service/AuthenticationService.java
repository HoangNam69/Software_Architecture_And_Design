package g5.kttkpm.authenticationservice.service;

import g5.kttkpm.authenticationservice.payload.LoginPayload;
import g5.kttkpm.authenticationservice.response.JwtResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.scope}")
    private String scopes;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public JwtResponse login(LoginPayload loginPayload) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", loginPayload.username());
        formData.add("password", loginPayload.password());
        formData.add("scope", scopes);
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        // Create a response with default values
        JwtResponse response = new JwtResponse();
        
        try {
            // Send request and get response
            JwtResponse keycloakResponse = restTemplate.postForObject(tokenUrl, requestEntity, JwtResponse.class);
            
            // Copy values from Keycloak response to our response
            if (keycloakResponse != null) {
                response.setAccessToken(keycloakResponse.getAccessToken());
                response.setExpiresIn(keycloakResponse.getExpiresIn());
                response.setRefreshExpiresIn(keycloakResponse.getRefreshExpiresIn());
                response.setRefreshToken(keycloakResponse.getRefreshToken());
                response.setTokenType(keycloakResponse.getTokenType());
                
                // Set status to true for successful login
                response.setStatus(true);
                response.setMessage("Login successful");
            }
        } catch (HttpClientErrorException e) {
            // Set status to false and provide error message on failure
            response.setStatus(false);
            
            // Determine the cause of the error based on status code
            if (e.getStatusCode().value() == 401) {
                response.setMessage("Invalid username or password");
            } else if (e.getStatusCode().value() == 400) {
                response.setMessage("Invalid request parameters");
            } else {
                response.setMessage("Authentication failed: " + e.getMessage());
            }
        } catch (Exception e) {
            // Handle any other exceptions
            response.setStatus(false);
            response.setMessage("Authentication service error: " + e.getMessage());
        }
        
        return response;
    }
    
    public void logout(String refreshToken) {
        String logoutUrl = issuerUri + "/protocol/openid-connect/logout";
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        // Send request
        restTemplate.postForEntity(logoutUrl, requestEntity, Void.class);
    }
}
