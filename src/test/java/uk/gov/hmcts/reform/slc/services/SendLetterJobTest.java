package uk.gov.hmcts.reform.slc.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageProcessor;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SendLetterJobTest {

    @Mock private MessageProcessor messageProcessor;
    @Mock private FtpAvailabilityChecker availabilityChecker;

    @Test
    public void should_NOT_process_message_if_ftps_is_not_available() {
        given(availabilityChecker.isFtpAvailable(any())).willReturn(false);
        SendLetterJob job = new SendLetterJob(messageProcessor, availabilityChecker);

        job.run();

        verify(messageProcessor, never()).process();
    }

    @Test
    public void should_process_message_if_ftps_is_available() {
        given(availabilityChecker.isFtpAvailable(any())).willReturn(true);
        SendLetterJob job = new SendLetterJob(messageProcessor, availabilityChecker);

        job.run();

        verify(messageProcessor, times(1)).process();
    }
}
