package iuh.fit.reportservice.controllers;

import iuh.fit.reportservice.dtos.request.ReportRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @PostMapping("/export-pdf")
    public ResponseEntity<?> exportPDFReport(@RequestBody ReportRequestDTO reportRequestDTO) {
        return ResponseEntity.ok(reportRequestDTO);
    }
}
