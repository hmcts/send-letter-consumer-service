package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.slc.services.SendLetterService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class MessageProcessorTest {

    private MessageProcessor processor;

    @Mock private IMessageReceiver messageReceiver;
    @Mock private SendLetterService sendLetterService;
    @Mock private IMessage message;

    @Before
    public void setUp() throws Exception {
        this.processor = new MessageProcessor(() -> messageReceiver, sendLetterService);
    }

    @Test
    public void should_complete_message_if_passed_function_returns_success() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(SUCCESS);
        given(messageReceiver.receive()).willReturn(message);

        // when
        processor.handle();

        // then
        verify(messageReceiver, times(1)).complete(any());
    }

    @Test
    public void should_send_message_to_deadletter_if_passed_function_returns_failure() throws Exception {
        // given
        given(sendLetterService.send(any())).willReturn(FAILURE);
        given(messageReceiver.receive()).willReturn(message);

        // when
        processor.handle();

        // then
        verify(messageReceiver, times(1)).deadLetter(any());
    }

    @Test
    public void should_not_call_action_if_there_are_no_messages_on_queue() throws Exception {
        // given
        given(messageReceiver.receive()).willReturn(null);

        // when
        processor.handle();

        // then
        verify(sendLetterService, never()).send(any());
    }
}
