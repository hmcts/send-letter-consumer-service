package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

@Component
public class SendLetterProducerHealthIndicator extends AbstractHealthIndicator {

    private final SendLetterClient client;

    public SendLetterProducerHealthIndicator(SendLetterClient client) {
        this.client = client;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Health health = client.serviceHealthy();
        builder.status(health.getStatus());
        health.getDetails().forEach(builder::withDetail);
    }
}
