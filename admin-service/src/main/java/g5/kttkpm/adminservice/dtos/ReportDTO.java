package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ReportDTO {

    private String startDate;
    private String endDate;
    private String currentDate;
    private Double totalRevenue;
    private List<ReportItemRequestDTO> reportItems;

    @Data
    private static class ReportItemRequestDTO {
        private String key;
        private String productName;
        private Integer quantity;
        private Double revenue;
    }
}
