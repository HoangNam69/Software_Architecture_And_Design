package g5.kttkpm.authenticationservice.service;

import g5.kttkpm.authenticationservice.payload.RegistrationPayload;
import g5.kttkpm.authenticationservice.response.JwtResponse;
import g5.kttkpm.authenticationservice.response.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

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
    
    @Autowired
    private RegistrationService registrationService;
    
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
    
    public String getRegistrationUrl() {
        // Keycloak broker endpoint for Google with registration
        String authEndpoint = issuerUri + "/protocol/openid-connect/auth";
        
        // Build the authorization URL with registration parameter
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authEndpoint)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", callbackUrl)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid profile email")
            .queryParam("kc_idp_hint", "google")
            .queryParam("prompt", "login") // Force showing the consent screen
            .queryParam("registration", "true"); // Tell Keycloak this is for registration
        
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
    
    /**
     * Registers a user with Google account information
     * If the Google account doesn't exist in Keycloak, create a new user
     *
     * @param code The authorization code from Google
     * @param phoneNumber The phone number to associate with the account
     * @return RegistrationResponse indicating status or failure
     */
    public RegistrationResponse registerWithGoogle(String code, String phoneNumber) {
        try {
            // First, exchange the code for tokens
            JwtResponse jwtResponse = exchangeCodeForTokens(code);
            
            if (jwtResponse == null || jwtResponse.getAccessToken() == null) {
                return new RegistrationResponse(false, "Failed to authenticate with Google", null);
            }
            
            // Get user info from the token
            Map<String, Object> userInfo = getUserInfoFromToken(jwtResponse.getAccessToken());
            
            if (userInfo == null) {
                return new RegistrationResponse(false, "Failed to retrieve user information from Google", null);
            }
            
            // Extract user details
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String sub = (String) userInfo.get("sub"); // Google's unique ID
            
            // Generate a username based on email (before the @ sign) and a random suffix
            String usernameBase = email.split("@")[0];
            String username = usernameBase + "_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Create a random password (user will authenticate via Google, not password)
            // String password = UUID.randomUUID().toString();
            
            // Create registration payload
            RegistrationPayload registrationPayload = new RegistrationPayload(
                username,
                email,
                phoneNumber,
                null,
                name,
                ""
            );
            
            // Register the user
            return registrationService.registerUser(registrationPayload);
        } catch (Exception e) {
            return new RegistrationResponse(false, "Error during Google registration: " + e.getMessage(), null);
        }
    }
    
    /**
     * Gets user information from an access token
     *
     * @param accessToken The access token
     * @return Map containing user info
     */
    private Map<String, Object> getUserInfoFromToken(String accessToken) {
        String userInfoUrl = issuerUri + "/protocol/openid-connect/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error getting user info from token: " + e.getMessage());
            return null;
        }
    }
}
