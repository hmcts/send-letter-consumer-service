package uk.gov.hmcts.reform.slc.services;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportParserTest {

    @Test
    public void should_parse_valid_csv_report() {
        String report =
            "\"Date\",\"Time\",\"Filename\"\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364001\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364002\n";

        List<LetterPrintStatus> result = new ReportParser().parse(report.getBytes());

        assertThat(result)
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

        List<LetterPrintStatus> result = new ReportParser().parse(report.getBytes());

        assertThat(result)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus("9364002", ZonedDateTime.parse("2018-01-01T10:30:53Z")));
    }

    @Test
    public void should_filter_out_rows_with_invalid_date() {
        String report =
            "\"Date\",\"Time\",\"Filename\"\n"
                + "20180101,10:30:53,TE5A_TE5B_9364001\n"
                + "2018-01-01,10:30:53,TE5A_TE5B_9364002\n";

        List<LetterPrintStatus> result = new ReportParser().parse(report.getBytes());

        assertThat(result)
            .usingFieldByFieldElementComparator()
            .containsExactly(new LetterPrintStatus("9364002", ZonedDateTime.parse("2018-01-01T10:30:53Z")));
    }
}
