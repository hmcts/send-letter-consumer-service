package uk.gov.hmcts.reform.slc.services;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.ParsedReport;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.Report;

import java.time.ZonedDateTime;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ReportParserTest {

    @Test
    public void should_parse_valid_csv_report() {
        String report =
            "\"Date\",\"Time\",\"Filename\"\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364001\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364002\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new LetterPrintStatus("9364001", ZonedDateTime.parse("2018-01-01T10:30:53Z")),
                new LetterPrintStatus("9364002", ZonedDateTime.parse("2018-01-01T10:30:53Z"))
            );
    }

    @Test
    public void should_filter_out_rows_with_invalid_file_name() {
        String report =
            "\"Date\",\"Time\",\"Filename\"\n"
                + "2018-01-01,10:30:53,invalidfilename\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364002\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus("9364002", ZonedDateTime.parse("2018-01-01T10:30:53Z")));
    }

    @Test
    public void should_filter_out_rows_with_invalid_date() {
        String report =
            "\"Date\",\"Time\",\"Filename\"\n"
                + "20180101,10:30:53,TE5A_TE5B_9364001\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364002\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus("9364002", ZonedDateTime.parse("2018-01-01T10:30:53Z")));
    }

    @Test
    public void should_parse_sample_report() throws Exception {
        byte[] report = toByteArray(getResource("report.csv"));

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report));

        assertThat(result.statuses).hasSize(11);
    }

    @Test
    public void should_throw_report_parsing_exception_when_csv_contains_semicolon_delimiter() {
        String report =
            "\"Date\";\"Time\";\"Filename\"\n"
                + "20180101;10:30:53;TE5A_TE5B_9364001\n"
                + "2018-01-01;10:30:53;TE5A_TE5B_9364002\n";

        Throwable exc = catchThrowable(() ->
            new ReportParser().parse(new Report("a.csv", report.getBytes())));

        assertThat(exc)
            .isInstanceOf(ReportParser.ReportParsingException.class);
    }
}
