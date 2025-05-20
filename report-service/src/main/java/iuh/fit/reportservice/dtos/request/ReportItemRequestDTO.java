package iuh.fit.reportservice.dtos.request;

public record ReportItemRequestDTO(
        String key, String productName, Integer quantity, Double revenue
) {
}
