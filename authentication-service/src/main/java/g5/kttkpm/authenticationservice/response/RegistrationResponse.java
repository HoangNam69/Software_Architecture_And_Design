package g5.kttkpm.authenticationservice.response;

public record RegistrationResponse(
    boolean status,
    String message,
    Object data
) {
}
