package g5.kttkpm.adminservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistrationPayload {

    private String username;
    private String email;

    @JsonProperty("phone")
    private String phone;

    private String password;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;
}