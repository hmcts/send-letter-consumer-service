package uk.gov.hmcts.reform.slc.health;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.slc.services.FtpAvailabilityChecker;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class FtpHealthIndicatorTest {

    @Mock
    private FtpAvailabilityChecker availabilityChecker;

    @Mock
    private FtpClient client;

    private HealthIndicator healthIndicator;

    @Before
    public void setUp() {
        healthIndicator = new FtpHealthIndicator(availabilityChecker, client);
    }

    @Test
    public void should_be_healthy_when_ftp_is_not_available() {
        given(availabilityChecker.isFtpAvailable(any(LocalTime.class))).willReturn(false);

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
        verify(client, never()).testConnection();
    }

    @Test
    public void should_be_healthy_when_ftp_is_available() {
        given(availabilityChecker.isFtpAvailable(any(LocalTime.class))).willReturn(true);

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void should_be_unhealthy_when_unable_to_connect_to_sftp() {
        given(availabilityChecker.isFtpAvailable(any(LocalTime.class))).willReturn(true);
        willThrow(new RuntimeException("hello")).given(client).testConnection();

        assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
