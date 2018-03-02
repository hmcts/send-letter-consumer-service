package uk.gov.hmcts.reform.slc.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.ParsedReport;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.Report;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    @Test
    public void should_not_remove_report_if_parsing_failed() {
        // given
        given(ftpAvailabilityChecker.isFtpAvailable(any())).willReturn(true);
        given(ftpClient.downloadReports()).willReturn(singletonList(new Report("FROM/1.csv", null)));
        given(parser.parse(any())).willThrow(new ReportParser.ReportParsingException(null));

        // when
        catchThrowable(() -> job.run());

        // then
        verify(ftpClient, times(0)).deleteReport(anyString());
    }

    @Test
    public void should_not_remove_report_if_sending_updates_failed() {
        // given
        given(ftpAvailabilityChecker.isFtpAvailable(any())).willReturn(true);
        given(ftpClient.downloadReports())
            .willReturn(singletonList(new Report("FROM/1.csv", null)));
        given(parser.parse(any()))
            .willReturn(
                new ParsedReport(
                    "FROM/1.csv",
                    asList(
                        new LetterPrintStatus("abc", now()),
                        new LetterPrintStatus("xyz", now())
                    )
                )
            );

        willThrow(RestClientException.class).given(sendLetterClient).updatePrintedAt(any());

        // when
        catchThrowable(() -> job.run());

        // then
        verify(ftpClient, times(0)).deleteReport(anyString());
    }

    @Test
    public void should_delete_file_if_parsing_and_api_calls_succeeded() {
        // given
        String filePath = "FROM/1.csv";
        given(ftpAvailabilityChecker.isFtpAvailable(any())).willReturn(true);

        given(ftpClient.downloadReports())
            .willReturn(singletonList(new Report(filePath, null)));

        given(parser.parse(any()))
            .willReturn(
                new ParsedReport(
                    filePath,
                    asList(
                        new LetterPrintStatus("abc", now()),
                        new LetterPrintStatus("xyz", now())
                    )
                )
            );

        // when
        job.run();

        // then
        verify(ftpClient, times(1)).deleteReport(filePath);
    }
}
