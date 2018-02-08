package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.SendLetterService;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.ConnectionException;

import java.util.function.Supplier;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final Supplier<IMessageReceiver> receiverProvider;
    private final SendLetterService sendLetterService;

    public MessageProcessor(
        Supplier<IMessageReceiver> receiverProvider,
        SendLetterService sendLetterService
    ) {
        this.receiverProvider = receiverProvider;
        this.sendLetterService = sendLetterService;
    }

    public void process() {
        try {
            IMessageReceiver messageReceiver = receiverProvider.get();

            try {
                IMessage message = messageReceiver.receive();

                if (message != null) {
                    // TODO log message received

                    MessageHandlingResult result = sendLetterService.send(message);

                    switch (result) {
                        case SUCCESS:
                            // TODO log message handled successfully

                            complete(messageReceiver, message);

                            break;
                        case FAILURE:
                            // TODO log message failed to handle - deadlettering

                            deadLetter(messageReceiver, message);

                            break;
                        default:
                            logger.error("Unknown message handling result: " + result);
                            deadLetter(messageReceiver, message);

                            break;
                    }
                } else {
                    logger.trace("No messages to process");
                }
            } catch (InterruptedException | ServiceBusException e) {
                // TODO log count of failed read

                logger.error("Unable to read message from queue", e);
            }

            messageReceiver.close();

        } catch (ConnectionException e) {
            // TODO log count of failed connect

            logger.error("Unable to connect to Service Bus", e);
        } catch (ServiceBusException e) {
            // TODO log count of failed close

            logger.error("Error closing connection");
        }
    }

    private void complete(IMessageReceiver receiver, IMessage msg) {
        try {
            receiver.complete(msg.getLockToken());
        } catch (InterruptedException | ServiceBusException e) {
            // TODO log failed to release message lock + count

            logger.error("Unable to mark message " + msg.getMessageId() + " as processed");
        }
    }

    private void deadLetter(IMessageReceiver receiver, IMessage msg) {
        try {
            receiver.deadLetter(msg.getLockToken());
        } catch (InterruptedException | ServiceBusException e) {
            // TODO log failed to dead letter the message + count

            logger.error("Unable to send message " + msg.getMessageId() + " to deadletter subqueue", e);
        }
    }
}
