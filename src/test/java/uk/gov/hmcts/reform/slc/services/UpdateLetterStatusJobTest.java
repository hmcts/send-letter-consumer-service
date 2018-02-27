package uk.gov.hmcts.reform.slc.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateLetterStatusJobTest {

    @Mock private FtpClient ftpClient;
    @Mock private FtpAvailabilityChecker ftpAvailabilityChecker;
    @Mock private SendLetterClient sendLetterClient;
    @Mock private ReportParser parser;

    private UpdateLetterStatusJob job;

    @Before
    public void setUp() {
        this.job = new UpdateLetterStatusJob(ftpClient, ftpAvailabilityChecker, sendLetterClient, parser);
    }

    @Test
    public void should_not_try_to_download_reports_if_ftp_is_not_available() {
        given(ftpAvailabilityChecker.isFtpAvailable(any())).willReturn(false);

        job.run();

        verify(ftpClient, never()).downloadReports();
    }

    @Test
    public void should_download_reports_if_ftp_is_available() {
        given(ftpAvailabilityChecker.isFtpAvailable(any())).willReturn(true);

        job.run();

        verify(ftpClient, times(1)).downloadReports();
    }
}
