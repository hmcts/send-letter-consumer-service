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
        try {
            IMessageReceiver messageReceiver = receiverProvider.get();
            Instant startReceiving = Instant.now();

            try {
                IMessage message = messageReceiver.receive();

                if (message != null) {
                    long tookReceiving = Duration.between(startReceiving, Instant.now()).toMillis();
                    long timeInQueue = Duration.between(message.getEnqueuedTimeUtc(), startReceiving).toNanos();
                    String messageId = message.getMessageId();

                    insights.trackMessageReceivedFromServiceBus(tookReceiving, true);
                    insights.trackMessageReceived(messageId, timeInQueue);

                    Instant startHandling = Instant.now();
                    MessageHandlingResult result = sendLetterService.send(message);
                    long tookHandling = Duration.between(startHandling, Instant.now()).toNanos();

                    switch (result) {
                        case SUCCESS:
                            insights.markMessageHandled(messageId, tookHandling);

                            complete(messageReceiver, message);

                            break;
                        case FAILURE:
                            insights.markMessageNotHandled(messageId, tookHandling);

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
                long tookReceiving = Duration.between(startReceiving, Instant.now()).toNanos();
                insights.trackMessageReceivedFromServiceBus(tookReceiving, false);
                insights.trackException(e);

                logger.error("Unable to read message from queue", e);
            }

            messageReceiver.close();

        } catch (ConnectionException e) {
            insights.trackException(e);

            logger.error("Unable to connect to Service Bus", e);
        } catch (ServiceBusException e) {
            insights.trackException(e);

            logger.error("Error closing connection");
        }
    }

    private void complete(IMessageReceiver receiver, IMessage msg) {
        Instant start = Instant.now();

        try {
            receiver.complete(msg.getLockToken());

            insights.trackMessageCompletedInServiceBus(Duration.between(start, Instant.now()).toMillis(), true);
        } catch (InterruptedException | ServiceBusException e) {
            insights.trackMessageCompletedInServiceBus(Duration.between(start, Instant.now()).toMillis(), false);

            logger.error("Unable to mark message " + msg.getMessageId() + " as processed");
        }
    }

    private void deadLetter(IMessageReceiver receiver, IMessage msg) {
        Instant start = Instant.now();

        try {
            receiver.deadLetter(msg.getLockToken());

            insights.trackMessageDeadLetteredInServiceBus(Duration.between(start, Instant.now()).toMillis(), true);
        } catch (InterruptedException | ServiceBusException e) {
            insights.trackMessageDeadLetteredInServiceBus(Duration.between(start, Instant.now()).toMillis(), false);

            logger.error("Unable to send message " + msg.getMessageId() + " to deadletter subqueue", e);
        }
    }
}
