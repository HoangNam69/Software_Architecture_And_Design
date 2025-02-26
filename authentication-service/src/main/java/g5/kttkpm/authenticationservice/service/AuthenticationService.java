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
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
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
        formData.add("scope", "openid profile");
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        // Send request and return response
        return restTemplate.postForObject(tokenUrl, requestEntity, JwtResponse.class);
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
