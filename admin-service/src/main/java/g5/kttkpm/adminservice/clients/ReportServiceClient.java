package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.ProductDTO;
import g5.kttkpm.adminservice.dtos.ReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReportServiceClient {

    private final WebClient webClient;

    public ReportServiceClient(WebClient.Builder webClientBuilder, @Value("${services.report}") String reportRoot) {
        this.webClient = webClientBuilder.baseUrl(reportRoot).build();
        log.info("Report service client initialized with baseUrl: {}", reportRoot);
    }

    // Export PDF
    public Mono<ReportDTO> exportRevenueReport(ReportDTO reportDTO) {
        return webClient.post()
                .uri("/export-pdf")
                .bodyValue(reportDTO)
                .retrieve()
                .bodyToMono(ReportDTO.class);
    }
    
    public void checkStatus() {
        webClient.get()
            .uri("/cb-status")
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
