package uk.gov.hmcts.reform.slc.health;

import com.microsoft.azure.servicebus.IMessageReceiver;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ServiceBusHealthIndicator extends AbstractHealthIndicator {

    private final Supplier<IMessageReceiver> receiverSupplier;

    public ServiceBusHealthIndicator(Supplier<IMessageReceiver> receiverSupplier) {
        this.receiverSupplier = receiverSupplier;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        receiverSupplier.get().close();

        builder.up();
    }
}
