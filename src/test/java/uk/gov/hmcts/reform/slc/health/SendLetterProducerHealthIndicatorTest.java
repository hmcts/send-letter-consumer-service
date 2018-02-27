package uk.gov.hmcts.reform.slc.health;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class SendLetterProducerHealthIndicatorTest {

    @Mock
    private SendLetterClient client;

    private HealthIndicator healthIndicator;

    @Before
    public void setUp() {
        healthIndicator = new SendLetterProducerHealthIndicator(client);
    }

    @Test
    public void should_be_healthy_when_send_letter_producer_service_is_healthy() {
        given(client.serviceHealthy()).willReturn(Health.up().build());

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void should_not_be_healthy_when_send_letter_producer_service_is_not_healthy() {
        given(client.serviceHealthy()).willReturn(Health.down().build());

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
