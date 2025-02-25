package g5.kttkpm.authenticationservice.service;

import g5.kttkpm.authenticationservice.response.JwtResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleAuthenticationService {
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    @Value("${app.auth.callback-url}")
    private String callbackUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String getLoginUrl() {
        // Keycloak broker endpoint for Google
        String authEndpoint = issuerUri + "/protocol/openid-connect/auth";
        
        // Build the authorization URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authEndpoint)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", callbackUrl)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid profile email")
            .queryParam("kc_idp_hint", "google"); // This tells Keycloak to use Google IDP directly
        
        return builder.toUriString();
    }
    
    public JwtResponse exchangeCodeForTokens(String code) {
        // Exchange authorization code for tokens
        String tokenEndpoint = issuerUri + "/protocol/openid-connect/token";
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("redirect_uri", callbackUrl);
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        // Send request and return response
        return restTemplate.postForObject(tokenEndpoint, requestEntity, JwtResponse.class);
    }
}
