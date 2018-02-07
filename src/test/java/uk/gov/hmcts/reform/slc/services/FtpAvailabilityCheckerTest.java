package uk.gov.hmcts.reform.slc.services;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalTime;

import static java.time.LocalTime.now;
import static java.util.Arrays.asList;

public class FtpAvailabilityCheckerTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void should_indicate_whether_ftp_is_available() {
        test("17:30", "18:30", time(18, 10), false, "During downtime");
        test("17:30", "18:30", time(22, 15), true, "After downtime");
        test("17:30", "18:30", time(11, 50), true, "Before downtime");
        test("17:30", "18:30", time(17, 30), false, "Exactly at downtime start");
        test("17:30", "18:30", time(18, 30), false, "Exactly at downtime end");
        test("23:00", "01:00", time(14, 15), true, "Outside of overnight downtime");
        test("23:00", "01:00", time(23, 59), false, "During overnight downtime");
    }

    @Test
    public void should_throw_an_exception_if_times_are_invalid() {
        asList("", "foo", "11am", "midnight", null)
            .forEach(invalidTime -> {
                // check with invalid time as 'from'...
                softly.assertThatThrownBy(
                    () -> new FtpAvailabilityChecker(invalidTime, "11:00").ftpAvailable(now())
                ).isNotNull();

                // ... and 'to' parameter
                softly.assertThatThrownBy(
                    () -> new FtpAvailabilityChecker("11:00", invalidTime).ftpAvailable(now())
                ).isNotNull();
            });
    }

    @SuppressWarnings("checkstyle:linelength")
    private void test(String downtimeFrom, String downtimeTo, LocalTime timeToCheck, boolean expectedResult, String desc) {
        // given
        FtpAvailabilityChecker checker = new FtpAvailabilityChecker(downtimeFrom, downtimeTo);

        // when
        boolean result = checker.ftpAvailable(timeToCheck);

        // then
        softly.assertThat(result).as(desc).isEqualTo(expectedResult);
    }

    private LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
