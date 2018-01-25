package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.SendLetterJob;

import java.util.function.Function;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SendLetterJob.class);

    private final String connString;

    public MessageProcessor(@Value("${servicebus.connectionString}") String connString) {
        this.connString = connString;
    }

    public void handle(Function<IMessage, MessageHandlingResult> action) {

        IMessageReceiver messageReceiver;
        try {
            messageReceiver = ClientFactory.createMessageReceiverFromConnectionString(connString, ReceiveMode.PEEKLOCK);
        } catch (Exception e) {
            logger.error("Unable to connect to Service Bus", e);
            return;
        }

        IMessage message;
        try {
            message = messageReceiver.receive();
        } catch (InterruptedException | ServiceBusException e) {
            logger.error("Unable to read message from queue", e);
            return;
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
