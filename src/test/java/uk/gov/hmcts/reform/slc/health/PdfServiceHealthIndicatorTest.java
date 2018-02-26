package uk.gov.hmcts.reform.slc.health;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class PdfServiceHealthIndicatorTest {

    @Mock
    private PDFServiceClient client;

    private HealthIndicator healthIndicator;

    @Before
    public void setUp() {
        healthIndicator = new PdfServiceHealthIndicator(client);
    }

    @Test
    public void pdf_service_should_be_healthy() {
        given(client.serviceHealthy()).willReturn(Health.up().build());

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void pdf_service_should_not_be_healthy() {
        given(client.serviceHealthy()).willReturn(
            Health.down(new PDFServiceClientException("some error", null)).build()
        );

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error")
            .containsValue(PDFServiceClientException.class.getName() + ": some error");
    }
}
