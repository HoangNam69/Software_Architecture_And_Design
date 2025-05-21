package iuh.fit.reportservice.dtos.request;

import java.util.List;

public class ReportRequestDTO {
    private String startDate;
    private String endDate;
    private String currentDate;
    private Double totalRevenue;
    private List<ReportItemRequestDTO> reportItems;

    public ReportRequestDTO() {
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<ReportItemRequestDTO> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<ReportItemRequestDTO> reportItems) {
        this.reportItems = reportItems;
    }
}
