package iuh.fit.reportservice.dtos.request;

public class ReportItemRequestDTO {

    private String productName;
    private int quantity;
    private String revenue;

    public ReportItemRequestDTO() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }
}
