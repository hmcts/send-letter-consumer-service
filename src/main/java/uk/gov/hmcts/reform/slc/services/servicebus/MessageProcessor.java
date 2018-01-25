package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.SendLetterJob;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.ConnectionException;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final Supplier<IMessageReceiver> receiverProvider;

    public MessageProcessor(Supplier<IMessageReceiver> receiverProvider) {
        this.receiverProvider = receiverProvider;
    }

    /**
     * Reads message from a queue and passes is as an argument to action.
     */
    public void handle(Function<IMessage, MessageHandlingResult> action) {

        IMessageReceiver messageReceiver;

        try {
            messageReceiver = receiverProvider.get();
        } catch (ConnectionException e) {
            logger.error("Unable to connect to Service Bus", e);
            return;
        }

        IMessage message = null;
        try {
            message = messageReceiver.receive();
        } catch (InterruptedException | ServiceBusException e) {
            logger.error("Unable to read message from queue", e);
        }

        if (message != null) {

            MessageHandlingResult result = action.apply(message);
            if (result == MessageHandlingResult.SUCCESS) {
                try {
                    messageReceiver.complete(message.getLockToken());
                } catch (InterruptedException | ServiceBusException e) {
                    logger.error("Unable to mark message " + message.getMessageId() + " as processed");
                }
            } else {
                try {
                    messageReceiver.deadLetter(message.getLockToken());
                } catch (InterruptedException | ServiceBusException e) {
                    logger.error("Unable to send message " + message.getMessageId() + " to deadletter subqueue", e);
                }
            }
        } else {
            logger.trace("No messages to process");
        }

        try {
            messageReceiver.close();
        } catch (ServiceBusException e) {
            logger.error("Error closing connection");
        }
    }
}
