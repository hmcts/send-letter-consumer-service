package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.SendLetterService;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class MessageProcessorTest {

    private MessageProcessor processor;

    @Mock private IMessageReceiver messageReceiver;
    @Mock private SendLetterService sendLetterService;
    @Mock private IMessage message;
    @Mock private AppInsights insights;

    @Before
    public void setUp() {
        processor = new MessageProcessor(() -> messageReceiver, sendLetterService);

        ReflectionTestUtils.setField(processor, "insights", insights);
    }

    @Test
    public void should_complete_message_if_passed_function_returns_success() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(SUCCESS);
        given(messageReceiver.receive()).willReturn(message);
        given(message.getEnqueuedTimeUtc()).willReturn(Instant.now().minusSeconds(100));

        // when
        processor.process();

        // then
        verify(messageReceiver).complete(any());
        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(true));
        verify(insights).trackMessageReceived(anyString(), anyLong());
        verify(insights).markMessageHandled(anyString(), anyLong());
        verify(insights).trackMessageCompletedInServiceBus(any(Duration.class), eq(true));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_send_message_to_deadletter_if_passed_function_returns_failure() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(FAILURE);
        given(messageReceiver.receive()).willReturn(message);
        given(message.getEnqueuedTimeUtc()).willReturn(Instant.now().minusSeconds(100));

        // when
        processor.process();

        // then
        verify(messageReceiver).deadLetter(any());
        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(true));
        verify(insights).trackMessageReceived(anyString(), anyLong());
        verify(insights).markMessageNotHandled(anyString(), anyLong());
        verify(insights).trackMessageDeadLetteredInServiceBus(any(Duration.class), eq(true));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_not_call_action_if_there_are_no_messages_on_queue() throws Exception {
        // given
        given(messageReceiver.receive()).willReturn(null);

        // when
        processor.process();

        // then
        verify(sendLetterService, never()).send(any());
        verifyNoMoreInteractions(insights);
    }
}
