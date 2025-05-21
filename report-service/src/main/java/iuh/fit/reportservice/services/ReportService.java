package iuh.fit.reportservice.services;

import iuh.fit.reportservice.dtos.request.ReportItemRequestDTO;
import iuh.fit.reportservice.dtos.request.ReportRequestDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    public byte[] exportReport(ReportRequestDTO requestDTO) throws Exception {
        if (requestDTO == null || requestDTO.getReportItems() == null) {
            throw new IllegalArgumentException("Report data cannot be null");
        }

        String[] currentDate = requestDTO.getCurrentDate().split("-");
//        log.info("Current day: " + currentDate[0] + "Current month: " + currentDate[1] + "Current year: " + currentDate[2]);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", requestDTO.getStartDate());
        parameters.put("endDate", requestDTO.getEndDate());
        parameters.put("currentDayDate", currentDate[2]);
        parameters.put("currentMonthDate", currentDate[1]);
        parameters.put("currentYearDate", currentDate[0]);
        parameters.put("totalRevenue", requestDTO.getTotalRevenue());

        List<ReportItemRequestDTO> items = requestDTO.getReportItems().stream()
                .map(i -> {
                    ReportItemRequestDTO ri = new ReportItemRequestDTO();
                    ri.setProductName(i.getProductName());
                    ri.setQuantity(i.getQuantity());
                    ri.setRevenue(i.getRevenue());
                    return ri;
                }).collect(Collectors.toList());

        JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(items);
        parameters.put("TABLE_DATA_SOURCE", itemsDataSource);

        try (InputStream reportStream = getClass().getResourceAsStream("/template/jasper/revenue_report.jasper")) {
            if (reportStream == null) {
                throw new FileNotFoundException("Report template not found at /template/jasper/revenue_report.jasper");
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }
}
