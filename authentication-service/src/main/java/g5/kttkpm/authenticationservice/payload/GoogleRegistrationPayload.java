package g5.kttkpm.authenticationservice.payload;

public record GoogleRegistrationPayload(
    String code,
    String phoneNumber
) {
}
