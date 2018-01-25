package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
        try {
            IMessageReceiver messageReceiver = receiverProvider.get();
            try {
                IMessage message = messageReceiver.receive();
                if (message != null) {
                    MessageHandlingResult result = action.apply(message);
                    switch (result) {
                        case SUCCESS:
                            complete(messageReceiver, message);
                            break;
                        case FAILURE:
                            deadLetter(messageReceiver, message);
                            break;
                        default:
                            logger.error("Unknown message handling result: " + result);
                            deadLetter(messageReceiver, message);
                    }
                } else {
                    logger.trace("No messages to process");
                }
            } catch (InterruptedException | ServiceBusException e) {
                logger.error("Unable to read message from queue", e);
            }

            messageReceiver.close();

        } catch (ConnectionException e) {
            logger.error("Unable to connect to Service Bus", e);
        } catch (ServiceBusException e) {
            logger.error("Error closing connection");
        }
    }

    private void complete(IMessageReceiver receiver, IMessage msg) {
        try {
            receiver.complete(msg.getLockToken());
        } catch (InterruptedException | ServiceBusException e) {
            logger.error("Unable to mark message " + msg.getMessageId() + " as processed");
        }
    }

    private void deadLetter(IMessageReceiver receiver, IMessage msg) {
        try {
            receiver.deadLetter(msg.getLockToken());
        } catch (InterruptedException | ServiceBusException e) {
            logger.error("Unable to send message " + msg.getMessageId() + " to deadletter subqueue", e);
        }
    }
}
