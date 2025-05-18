package g5.kttkpm.adminservice.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CircuitBreakerResponse(
    @JsonProperty("service_name") String serviceName,
    String status,
    @JsonProperty("is_online") boolean isOnline) {
}
