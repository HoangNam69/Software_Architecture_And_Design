package g5.kttkpm.authenticationservice.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationPayload(
    String username,
    String email,
    @JsonProperty("phone_number")
    String phoneNumber,
    String password
) {
}
