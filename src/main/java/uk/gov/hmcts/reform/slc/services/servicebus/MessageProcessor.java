package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.SendLetterService;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.ConnectionException;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final Supplier<IMessageReceiver> receiverProvider;
    private final SendLetterService sendLetterService;

    @Autowired
    private AppInsights insights;

    public MessageProcessor(
        Supplier<IMessageReceiver> receiverProvider,
        SendLetterService sendLetterService
    ) {
        this.receiverProvider = receiverProvider;
        this.sendLetterService = sendLetterService;
    }

    public void process() {
        logger.info("Processing messages from the queue...");

        IMessageReceiver messageReceiver = null;

        try {
            messageReceiver = receiverProvider.get();
            IMessage message = receiveMessage(messageReceiver);

            while (message != null) {
                sendLetter(messageReceiver, message);
                message = receiveMessage(messageReceiver);
            }

            logger.info("Finished processing queue");
        } catch (ConnectionException e) {
            logger.error("Unable to connect to Service Bus", e);
        } finally {
            if (messageReceiver != null) {
                try {
                    messageReceiver.close();
                } catch (ServiceBusException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    private IMessage receiveMessage(IMessageReceiver messageReceiver) {
        Instant receiveStartTime = Instant.now();

        try {
            IMessage message = messageReceiver.receive();

            if (message != null) {
                Duration tookReceiving = Duration.between(receiveStartTime, Instant.now());
                Duration timeInQueue = Duration.between(message.getEnqueuedTimeUtc(), receiveStartTime);
                String messageId = message.getMessageId();

                insights.trackMessageReceivedFromServiceBus(tookReceiving, true);
                insights.trackMessageReceived(messageId, timeInQueue);
            } else {
                logger.info("No messages to process");
            }

            return message;
        } catch (InterruptedException | ServiceBusException e) {
            // TODO: change the event to "MessageReceivingFailed"
            Duration tookReceiving = Duration.between(receiveStartTime, Instant.now());
            insights.trackMessageReceivedFromServiceBus(tookReceiving, false);
            logger.error("Unable to read message from queue", e);

            return null;
        }
    }

    private void sendLetter(IMessageReceiver messageReceiver, IMessage message) {
        Instant startHandling = Instant.now();
        MessageHandlingResult result = sendLetterService.send(message);
        Duration tookHandling = Duration.between(startHandling, Instant.now());

        switch (result) {
            case SUCCESS:
                insights.markMessageHandled(message.getMessageId(), tookHandling);
                complete(messageReceiver, message);
                break;
            case FAILURE:
                insights.markMessageNotHandled(message.getMessageId(), tookHandling);
                deadLetter(messageReceiver, message);
                break;
            default:
                logger.error("Unknown message handling result: {}", result);
                deadLetter(messageReceiver, message);
                break;
        }
    }

    private void complete(IMessageReceiver receiver, IMessage msg) {
        Instant start = Instant.now();

        try {
            receiver.complete(msg.getLockToken());

            insights.trackMessageCompletedInServiceBus(Duration.between(start, Instant.now()), true);
        } catch (InterruptedException | ServiceBusException e) {
            insights.trackMessageCompletedInServiceBus(Duration.between(start, Instant.now()), false);

            logger.error("Unable to mark message " + msg.getMessageId() + " as processed", e);
        }
    }

    private void deadLetter(IMessageReceiver receiver, IMessage msg) {
        Instant start = Instant.now();

        try {
            receiver.deadLetter(msg.getLockToken());

            insights.trackMessageDeadLetteredInServiceBus(Duration.between(start, Instant.now()), true);
        } catch (InterruptedException | ServiceBusException e) {
            insights.trackMessageDeadLetteredInServiceBus(Duration.between(start, Instant.now()), false);

            logger.error("Unable to send message " + msg.getMessageId() + " to deadletter subqueue", e);
        }
    }
}
