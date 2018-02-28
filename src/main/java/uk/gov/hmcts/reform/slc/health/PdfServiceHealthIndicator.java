package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@Component
public class PdfServiceHealthIndicator implements HealthIndicator {

    private final PDFServiceClient client;

    public PdfServiceHealthIndicator(PDFServiceClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        return client.serviceHealthy();
    }
}
