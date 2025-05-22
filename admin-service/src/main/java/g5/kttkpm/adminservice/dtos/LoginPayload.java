package g5.kttkpm.adminservice.dtos;

import lombok.Data;

@Data
public class LoginPayload {
    private String username;
    private String password;
}