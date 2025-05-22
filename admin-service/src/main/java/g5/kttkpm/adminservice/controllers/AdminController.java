package g5.kttkpm.adminservice.controllers;

import g5.kttkpm.adminservice.clients.ReportServiceClient;
import g5.kttkpm.adminservice.dtos.ReportDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/report")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final ReportServiceClient reportServiceClient;

    @PostMapping("/export-pdf")
    public ResponseEntity<?> exportPDFReport(@RequestBody ReportDTO reportRequestDTO) {
        return ResponseEntity.ok(reportServiceClient.exportRevenueReport(reportRequestDTO).block());
    }
}
