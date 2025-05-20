package iuh.fit.reportservice.dtos.request;

import java.util.List;

public record ReportRequestDTO(
        String startDate, String endDate, String currentDate, Double totalRevenue,
        List<ReportItemRequestDTO> reportItems
) {

}
