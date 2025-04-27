package g5.kttkpm.adminservice.responses;

import lombok.Data;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}