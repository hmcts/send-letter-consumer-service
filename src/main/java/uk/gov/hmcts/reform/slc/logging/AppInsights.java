package uk.gov.hmcts.reform.slc.logging;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

import java.util.Map;

import static java.util.Collections.singletonMap;

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

    public void trackMessageReceived(String messageId, java.time.Duration duration) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_RECEIVED,
            singletonMap("messageId", messageId),
            singletonMap("enqueuedInMillis", (double) duration.toMillis())
        );
    }

    public void markMessageHandled(String messageId, java.time.Duration duration) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_HANDLED_SUCCESSFULLY,
            singletonMap("messageId", messageId),
            singletonMap("handledInMillis", (double) duration.toMillis())
        );
    }

    public void markMessageNotHandled(String messageId, java.time.Duration duration) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_HANDLED_UNSUCCESSFULLY,
            singletonMap("messageId", messageId),
            singletonMap("handledInMillis", (double) duration.toMillis())
        );
    }

    public void trackMessageMappedToLetter(String messageId, String serviceName, String template, long bodyLength) {
        Map<String, String> properties = ImmutableMap.of(
            "messageId", messageId,
            "service", serviceName,
            "template", template
        );

        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_SUCCESSFULLY,
            properties,
            singletonMap("messageSize", (double) bodyLength)
        );
    }

    public void trackMessageMappedToNull(String messageId) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_EMPTY,
            singletonMap("messageId", messageId),
            null
        );
    }

    public void trackMessageMappedToInvalid(String messageId, long bodyLength) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_INVALID,
            singletonMap("messageId", messageId),
            singletonMap("messageSize", (double) bodyLength)
        );
    }

    public void trackMessageNotMapped(String messageId, long bodyLength) {
        telemetry.trackEvent(
            AppEvent.MESSAGE_MAPPED_UNSUCCESSFULLY,
            singletonMap("messageId", messageId),
            singletonMap("messageSize", (double) bodyLength)
        );
    }

    // EXCEPTIONS

    public void trackException(Exception exception) {
        telemetry.trackException(exception);
    }
}
