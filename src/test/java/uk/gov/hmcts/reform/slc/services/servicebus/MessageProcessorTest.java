package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.SendLetterService;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.ConnectionException;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    @Mock private Supplier<IMessageReceiver> receiverProvider;

    @Before
    public void setUp() {
        processor = new MessageProcessor(() -> messageReceiver, sendLetterService);

        ReflectionTestUtils.setField(processor, "insights", insights);
    }

    @Test
    public void should_complete_message_if_passed_function_returns_success() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(SUCCESS);
        given(messageReceiver.receive()).willReturn(message).willReturn(null);
        given(message.getEnqueuedTimeUtc()).willReturn(Instant.now().minusSeconds(100));

        // when
        processor.process();

        // then
        verify(messageReceiver).complete(any());
        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(true));
        verify(insights).trackMessageReceived(anyString(), any(Duration.class));
        verify(insights).markMessageHandled(anyString(), any(Duration.class));
        verify(insights).trackMessageCompletedInServiceBus(any(Duration.class), eq(true));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_send_message_to_deadletter_if_passed_function_returns_failure() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(FAILURE);
        given(messageReceiver.receive()).willReturn(message).willReturn(null);
        given(message.getEnqueuedTimeUtc()).willReturn(Instant.now().minusSeconds(100));

        // when
        processor.process();

        // then
        verify(messageReceiver).deadLetter(any());
        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(true));
        verify(insights).trackMessageReceived(anyString(), any(Duration.class));
        verify(insights).markMessageNotHandled(anyString(), any(Duration.class));
        verify(insights).trackMessageDeadLetteredInServiceBus(any(Duration.class), eq(true));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_process_all_messages_from_queue() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(SUCCESS);
        given(messageReceiver.receive()).willReturn(message, message, message, null);
        given(message.getEnqueuedTimeUtc()).willReturn(Instant.now().minusSeconds(100));

        // when
        processor.process();

        // then
        int numberOfMessages = 3;
        verify(messageReceiver, times(numberOfMessages)).complete(any());
        verify(insights, times(numberOfMessages)).trackMessageReceivedFromServiceBus(any(), eq(true));
        verify(insights, times(numberOfMessages)).trackMessageReceived(anyString(), any());
        verify(insights, times(numberOfMessages)).markMessageHandled(anyString(), any());
        verify(insights, times(numberOfMessages)).trackMessageCompletedInServiceBus(any(), eq(true));
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

    @Test
    public void should_not_process_message_when_connection_exception_is_thrown_on_acquiring_sb_queue_connection() {
        //given
        processor = new MessageProcessor(receiverProvider, sendLetterService);
        ReflectionTestUtils.setField(processor, "insights", insights);

        doThrow(ConnectionException.class).when(receiverProvider).get();

        //when
        Throwable exception = catchThrowable(() -> {
            processor.process();
        });

        //then
        assertThat(exception).isNull();

        verify(sendLetterService, never()).send(any());
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_not_process_message_when_interrupted_exception_is_thrown_on_retrieving_message_from_sb_queue()
        throws Exception {
        // given
        doThrow(InterruptedException.class).when(messageReceiver).receive();

        //when
        Throwable exception = catchThrowable(() -> {
            processor.process();
        });

        // then
        assertThat(exception).isNull();

        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(false));
        verify(sendLetterService, never()).send(any());
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_not_process_message_when_servicebus_exception_is_thrown_on_retrieving_message_from_sb_queue()
        throws Exception {
        // given
        doThrow(ServiceBusException.class).when(messageReceiver).receive();

        //when
        Throwable exception = catchThrowable(() -> {
            processor.process();
        });

        // then
        assertThat(exception).isNull();

        verify(insights).trackMessageReceivedFromServiceBus(any(Duration.class), eq(false));
        verify(sendLetterService, never()).send(any());
        verifyNoMoreInteractions(insights);
    }
}
