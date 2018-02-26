package uk.gov.hmcts.reform.slc.health;

import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.ConnectionException;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusHealthIndicatorTest {

    @Mock
    private Supplier<IMessageReceiver> receiverSupplier;

    @Mock
    private IMessageReceiver receiver;

    private HealthIndicator healthIndicator;

    @Before
    public void setUp() {
        healthIndicator = new ServiceBusHealthIndicator(receiverSupplier);
    }

    @Test
    public void should_be_healthy_if_we_can_connect_to_queue() {
        given(receiverSupplier.get()).willReturn(receiver);

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void should_be_unhealthy_when_failed_to_close_connection() throws ServiceBusException {
        given(receiverSupplier.get()).willReturn(receiver);
        willThrow(ServiceBusException.class).given(receiver).close();

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error")
            .containsValue(ConnectionException.class.getName() + ": Unable to close the queue");
    }

    @Test
    public void should_be_unhealthy_when_failed_to_connect() {
        willThrow(ConnectionException.class).given(receiverSupplier).get();

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
