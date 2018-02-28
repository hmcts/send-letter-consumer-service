package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

@Component
public class SendLetterProducerHealthIndicator implements HealthIndicator {

    private final SendLetterClient client;

    public SendLetterProducerHealthIndicator(SendLetterClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        return client.serviceHealthy();
    }
}
