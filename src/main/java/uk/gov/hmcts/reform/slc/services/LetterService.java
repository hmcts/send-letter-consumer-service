package uk.gov.hmcts.reform.slc.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.io.IOException;

@Service
public class LetterService {

    private static final Logger logger = LoggerFactory.getLogger(LetterService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String connString;

    public LetterService(@Value("${servicebus.connectionString}") String connString) {
        this.connString = connString;
    }

    @Scheduled(fixedDelay = 30_000)
    public void run() {

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
            try {
                Letter letter = objectMapper.readValue(message.getBody(), Letter.class);
                if (letter != null) {
                    logger.trace("Processing letter: " + letter);

                    // TODO: generate PDF
                    // TODO: send PDF to Xerox
                    messageReceiver.complete(message.getLockToken());
                } else {
                    logger.error("Empty message");
                    moveToDeadletterSubqueue(messageReceiver, message);
                }
            } catch (ServiceBusException | InterruptedException e) {
                logger.error("Unable to mark message " + message.getMessageId() + " as completed");
                moveToDeadletterSubqueue(messageReceiver, message);
            } catch (IOException e) {
                logger.error("Unable to deserialize message " + message.getMessageId());
                moveToDeadletterSubqueue(messageReceiver, message);
            }
        } else {
            logger.debug("Queue empty");
        }

        try {
            messageReceiver.close();
        } catch (ServiceBusException e) {
            logger.error("Error closing connection to ");
        }
    }

    private void moveToDeadletterSubqueue(IMessageReceiver receiver, IMessage message) {
        try {
            receiver.deadLetter(message.getLockToken());
        } catch (InterruptedException | ServiceBusException deadletterExc) {
            logger.error("Unable to send message to deadletter subqueue", deadletterExc);
        }
    }
}
