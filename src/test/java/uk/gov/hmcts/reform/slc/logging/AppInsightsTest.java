package uk.gov.hmcts.reform.slc.logging;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppInsightsTest {

    private static final java.time.Duration TIME_TOOK = java.time.Duration.ofMinutes(1);

    private static final String MESSAGE_ID_KEY = "messageId";

    private static final String MESSAGE_SIZE_KEY = "messageSize";

    private static final String MESSAGE_ID = "some message id";

    private static final long NOT_MANY_NANOS = 1_000;

    private static final String SERVICE_NAME = "some service";

    private static final String TEMPLATE = "template";

    private static final long BODY_LENGTH = 1_000;

    private final TelemetryContext context = new TelemetryContext();

    @Mock
    private TelemetryClient client;

    private AppInsights insights;

    @Before
    public void initInsights() {
        context.setInstrumentationKey("some-key");
        when(client.getContext()).thenReturn(context);

        insights = new AppInsights(client);
    }

    // DEPENDENCIES

    @Test
    public void should_track_message_received_from_service_bus() {
        insights.trackMessageReceivedFromServiceBus(TIME_TOOK, true);
        insights.trackMessageReceivedFromServiceBus(TIME_TOOK, false);

        verify(client, times(2)).trackDependency(
            eq(AppDependency.SERVICE_BUS),
            eq(AppDependencyCommand.SERVICE_BUS_MESSAGE_RECEIVED),
            any(Duration.class),
            anyBoolean()
        );
    }

    @Test
    public void should_track_message_completed_in_service_bus() {
        insights.trackMessageCompletedInServiceBus(TIME_TOOK, true);
        insights.trackMessageCompletedInServiceBus(TIME_TOOK, false);

        verify(client, times(2)).trackDependency(
            eq(AppDependency.SERVICE_BUS),
            eq(AppDependencyCommand.SERVICE_BUS_MESSAGE_COMPLETED),
            any(Duration.class),
            anyBoolean()
        );
    }

    @Test
    public void should_track_message_dead_lettered_in_service_bus() {
        insights.trackMessageDeadLetteredInServiceBus(TIME_TOOK, true);
        insights.trackMessageDeadLetteredInServiceBus(TIME_TOOK, false);

        verify(client, times(2)).trackDependency(
            eq(AppDependency.SERVICE_BUS),
            eq(AppDependencyCommand.SERVICE_BUS_DEAD_LETTERED),
            any(Duration.class),
            anyBoolean()
        );
    }

    @Test
    public void should_track_pdf_generator() {
        insights.trackPdfGenerator(TIME_TOOK, true);
        insights.trackPdfGenerator(TIME_TOOK, false);

        verify(client, times(2)).trackDependency(
            eq(AppDependency.PDF_GENERATOR),
            eq(AppDependencyCommand.GENERATED_PDF_FROM_HTML),
            any(Duration.class),
            anyBoolean()
        );
    }

    @Test
    public void should_track_ftp_upload() {
        insights.trackFtpUpload(TIME_TOOK, true);
        insights.trackFtpUpload(TIME_TOOK, false);

        verify(client, times(2)).trackDependency(
            eq(AppDependency.FTP_CLIENT),
            eq(AppDependencyCommand.FTP_FILE_UPLOADED),
            any(Duration.class),
            anyBoolean()
        );
    }

    // EVENTS

    @Test
    public void should_track_message_received() {
        insights.trackMessageReceived(MESSAGE_ID, NOT_MANY_NANOS);

        verify(client).trackEvent(
            AppEvent.MESSAGE_RECEIVED,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            Collections.singletonMap("enqueuedInNanos", (double) NOT_MANY_NANOS)
        );
    }

    @Test
    public void should_mark_message_handled() {
        insights.markMessageHandled(MESSAGE_ID, NOT_MANY_NANOS);

        verify(client).trackEvent(
            AppEvent.MESSAGE_HANDLED_SUCCESSFULLY,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            Collections.singletonMap("handledInNanos", (double) NOT_MANY_NANOS)
        );
    }

    @Test
    public void should_mark_message_not_handled() {
        insights.markMessageNotHandled(MESSAGE_ID, NOT_MANY_NANOS);

        verify(client).trackEvent(
            AppEvent.MESSAGE_HANDLED_UNSUCCESSFULLY,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            Collections.singletonMap("handledInNanos", (double) NOT_MANY_NANOS)
        );
    }

    @Test
    public void should_track_message_mapped_to_letter() {
        insights.trackMessageMappedToLetter(MESSAGE_ID, SERVICE_NAME, TEMPLATE, BODY_LENGTH);

        Map<String, String> properties = ImmutableMap.of(
            MESSAGE_ID_KEY, MESSAGE_ID,
            "service", SERVICE_NAME,
            "template", TEMPLATE
        );

        verify(client).trackEvent(
            AppEvent.MESSAGE_MAPPED_SUCCESSFULLY,
            properties,
            Collections.singletonMap(MESSAGE_SIZE_KEY, (double) BODY_LENGTH)
        );
    }

    @Test
    public void should_track_message_mapped_to_null() {
        insights.trackMessageMappedToNull(MESSAGE_ID);

        verify(client).trackEvent(
            AppEvent.MESSAGE_MAPPED_EMPTY,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            null
        );
    }

    @Test
    public void should_track_message_mapped_to_invalid() {
        insights.trackMessageMappedToInvalid(MESSAGE_ID, BODY_LENGTH);

        verify(client).trackEvent(
            AppEvent.MESSAGE_MAPPED_INVALID,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            Collections.singletonMap(MESSAGE_SIZE_KEY, (double) BODY_LENGTH)
        );
    }

    @Test
    public void should_track_message_not_mapped() {
        insights.trackMessageNotMapped(MESSAGE_ID, BODY_LENGTH);

        verify(client).trackEvent(
            AppEvent.MESSAGE_MAPPED_UNSUCCESSFULLY,
            Collections.singletonMap(MESSAGE_ID_KEY, MESSAGE_ID),
            Collections.singletonMap(MESSAGE_SIZE_KEY, (double) BODY_LENGTH)
        );
    }

    // EXCEPTIONS

    @Test
    public void should_track_any_exception() {
        insights.trackException(new NullPointerException("some exception"));

        verify(client).trackException(any(NullPointerException.class));
    }
}
