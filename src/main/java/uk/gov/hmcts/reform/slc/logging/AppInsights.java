package uk.gov.hmcts.reform.slc.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppInsights extends AbstractAppInsights {

    public AppInsights(TelemetryClient telemetryClient) {
        super(telemetryClient);
    }

    // DEPENDENCIES

    public void trackMessageReceivedFromServiceBus(long durationInMillis, boolean success) {
        telemetry.trackDependency(
            AppDependency.MESSAGE_RECEIVER_DEPENDENCY,
            AppDependency.MESSAGE_RECEIVED_COMMAND,
            new Duration(durationInMillis),
            success
        );
    }

    public void trackMessageCompletedInServiceBus(long durationInMillis, boolean success) {
        telemetry.trackDependency(
            AppDependency.MESSAGE_RECEIVER_DEPENDENCY,
            AppDependency.MESSAGE_COMPLETED_COMMAND,
            new Duration(durationInMillis),
            success
        );
    }

    public void trackMessageDeadLetteredInServiceBus(long durationInMillis, boolean success) {
        telemetry.trackDependency(
            AppDependency.MESSAGE_RECEIVER_DEPENDENCY,
            AppDependency.MESSAGE_DEAD_LETTER_COMMAND,
            new Duration(durationInMillis),
            success
        );
    }

    public void trackPdfGenerator(long durationInMillis, boolean success) {
        telemetry.trackDependency(
            AppDependency.PDF_GENERATOR_DEPENDENCY,
            AppDependency.GENERATE_PDF_FROM_HTML_COMMAND,
            new Duration(durationInMillis),
            success
        );
    }

    public void trackFtpUpload(long durationInMillis, boolean success) {
        telemetry.trackDependency(
            AppDependency.FTP_DEPENDENCY,
            AppDependency.FTP_FILE_UPLOAD_COMMAND,
            new Duration(durationInMillis),
            success
        );
    }

    // EVENTS

    public void trackMessageReceived(String messageId, long enqueuedInNanos) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_RECEIVED,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("enqueuedInNanos", (double) enqueuedInNanos));
    }

    public void markMessageHandled(String messageId, long handlingInNanos) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_HANDLED_SUCCESSFULLY,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("handledInNanos", (double) handlingInNanos)
        );
    }

    public void markMessageNotHandled(String messageId, long handlingInNanos) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_HANDLED_UNSUCCESSFULLY,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("handledInNanos", (double) handlingInNanos)
        );
    }

    public void trackMessageMappedToLetter(String messageId, String serviceName, String template, long bodyLength) {
        Map<String, String> properties = new HashMap<>();

        properties.put("messageId", messageId);
        properties.put("service", serviceName);
        properties.put("template", template);

        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_SUCCESSFULLY,
            properties,
            Collections.singletonMap("messageSize", (double) bodyLength)
        );
    }

    public void trackMessageMappedToNull(String messageId, long bodyLength) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_EMPTY,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("messageSize", (double) bodyLength)
        );
    }

    public void trackMessageMappedToInvalid(String messageId, long bodyLength) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_INVALID,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("messageSize", (double) bodyLength)
        );
    }

    public void trackMessageNotMapped(String messageId, long bodyLength) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_UNSUCCESSFULLY,
            Collections.singletonMap("messageId", messageId),
            Collections.singletonMap("messageSize", (double) bodyLength)
        );
    }

    // EXCEPTIONS

    public void trackException(Exception exception) {
        telemetry.trackException(exception);
    }
}
