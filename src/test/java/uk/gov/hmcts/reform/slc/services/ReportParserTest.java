package uk.gov.hmcts.reform.slc.services;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.ParsedReport;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.Report;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ReportParserTest {

    private static final ZonedDateTime expectedZonedDateTime = ZonedDateTime.of(
        2018, 3, 27, 16, 38, 0, 0,
        ZoneId.of("Z"));

    @Test
    public void should_parse_valid_csv_report() {
        String report =
            "\"StartDate\",\"StartTime\",\"InputFileName\"\n"
                + "27-03-2018,16:38,CMC001_cmcclaimstore_ff99f8ad-7ab8-43f8-9671-5397cbfa96a6.pdf\n"
                + "27-03-2018,16:38,CMC001_cmcclaimstore_ff88f8ad-8ab8-44f8-9672-5398cbfa96a7.pdf\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new LetterPrintStatus(
                    "ff99f8ad-7ab8-43f8-9671-5397cbfa96a6",
                    expectedZonedDateTime
                ),
                new LetterPrintStatus(
                    "ff88f8ad-8ab8-44f8-9672-5398cbfa96a7",
                    expectedZonedDateTime
                )
            );

        assertThat(result.allRowsParsed).isTrue();
    }

    @Test
    public void should_filter_out_rows_with_invalid_file_name() {
        String report =
            "\"StartDate\",\"StartTime\",\"InputFileName\"\n"
                + "27-03-2018,16:38,invalidfilename\n"
                + "27-03-2018,16:38,CMC001_cmcclaimstore_ff88f8ad-8ab8-44f8-9672-5398cbfa96a7.pdf\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus(
                "ff88f8ad-8ab8-44f8-9672-5398cbfa96a7",
                expectedZonedDateTime
            ));

        assertThat(result.allRowsParsed).isFalse();
    }

    @Test
    public void should_filter_out_rows_with_invalid_date() {
        String report =
            "\"StartDate\",\"StartTime\",\"InputFileName\"\n"
                + "20180101,16:38,CMC001_cmcclaimstore_ff99f8ad-7ab8-43f8-9671-5397cbfa96a6.pdf\n"
                + "27-03-2018,16:38,CMC001_cmcclaimstore_ff88f8ad-8ab8-44f8-9672-5398cbfa96a7.pdf\n";

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report.getBytes()));

        assertThat(result.statuses)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus(
                "ff88f8ad-8ab8-44f8-9672-5398cbfa96a7",
                expectedZonedDateTime
            ));

        assertThat(result.allRowsParsed).isFalse();
    }

    @Test
    public void should_parse_sample_report() throws Exception {
        byte[] report = toByteArray(getResource("report.csv"));

        ParsedReport result = new ReportParser().parse(new Report("a.csv", report));

        assertThat(result.statuses).hasSize(3);

        assertThat(result.allRowsParsed).isTrue();
    }

    @Test
    public void should_throw_report_parsing_exception_when_csv_contains_semicolon_delimiter() {
        String report =
            "\"StartDate\";\"StartTime\";\"InputFileName\"\n"
                + "27-03-2018;16:38;CMC001_cmcclaimstore_ff99f8ad-7ab8-43f8-9671-5397cbfa96a6.pdf\n"
                + "27-03-2018;16:38;CMC001_cmcclaimstore_ff88f8ad-8ab8-44f8-9672-5398cbfa96a7.pdf\n";

        Throwable exc = catchThrowable(() ->
            new ReportParser().parse(new Report("a.csv", report.getBytes())));

        assertThat(exc)
            .isInstanceOf(ReportParser.ReportParsingException.class);
    }
}
