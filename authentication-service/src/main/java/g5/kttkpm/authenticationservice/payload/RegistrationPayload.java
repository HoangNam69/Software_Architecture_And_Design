package g5.kttkpm.authenticationservice.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationPayload(
    String username,
    String email,
    @JsonProperty("phone")
    String phone,
    String password,
    
    @JsonProperty("first_name")
    String firstName,
    @JsonProperty("last_name")
    String lastName
) {
}
