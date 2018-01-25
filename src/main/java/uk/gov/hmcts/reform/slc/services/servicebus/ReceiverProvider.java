package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ReceiverProvider implements Supplier<IMessageReceiver> {

    private final String connString;

    public ReceiverProvider(
        @Value("${servicebus.connectionString}") String connString
    ) {
        this.connString = connString;
    }

    @Override
    public IMessageReceiver get() {
        try {
            return ClientFactory.createMessageReceiverFromConnectionString(connString, ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ConnectionException("Unable to connect to queue", e);
        }
    }
}
