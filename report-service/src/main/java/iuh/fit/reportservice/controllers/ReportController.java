package iuh.fit.reportservice.controllers;

import iuh.fit.reportservice.dtos.request.ReportRequestDTO;
import iuh.fit.reportservice.services.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<?> exportPDFReport(@RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            byte[] pdfBytes = reportService.exportReport(reportRequestDTO);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Thêm header để không xử lý nội dung PDF
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.add("Content-Disposition", "attachment; filename=revenue_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error generating report: " + e.getMessage());
        }
    }
}
