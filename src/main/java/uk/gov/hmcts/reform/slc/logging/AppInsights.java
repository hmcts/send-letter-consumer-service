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

    public void trackMessageReceivedFromServiceBus(java.time.Duration duration, boolean success) {
        telemetry.trackDependency(
            AppDependency.SERVICE_BUS,
            AppDependencyCommand.SERVICE_BUS_MESSAGE_RECEIVED,
            new Duration(duration.toMillis()),
            success
        );
    }

    public void trackMessageCompletedInServiceBus(java.time.Duration duration, boolean success) {
        telemetry.trackDependency(
            AppDependency.SERVICE_BUS,
            AppDependencyCommand.SERVICE_BUS_MESSAGE_COMPLETED,
            new Duration(duration.toMillis()),
            success
        );
    }

    public void trackMessageDeadLetteredInServiceBus(java.time.Duration duration, boolean success) {
        telemetry.trackDependency(
            AppDependency.SERVICE_BUS,
            AppDependencyCommand.SERVICE_BUS_DEAD_LETTERED,
            new Duration(duration.toMillis()),
            success
        );
    }

    public void trackPdfGenerator(java.time.Duration duration, boolean success) {
        telemetry.trackDependency(
            AppDependency.PDF_GENERATOR,
            AppDependencyCommand.GENERATED_PDF_FROM_HTML,
            new Duration(duration.toMillis()),
            success
        );
    }

    public void trackFtpUpload(java.time.Duration duration, boolean success) {
        telemetry.trackDependency(
            AppDependency.FTP_CLIENT,
            AppDependencyCommand.FTP_FILE_UPLOADED,
            new Duration(duration.toMillis()),
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
