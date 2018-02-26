package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@Component
public class PdfServiceHealthIndicator extends AbstractHealthIndicator {

    private final PDFServiceClient client;

    public PdfServiceHealthIndicator(PDFServiceClient client) {
        this.client = client;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Health health = client.serviceHealthy();
        builder.status(health.getStatus());
        health.getDetails().forEach(builder::withDetail);
    }
}
