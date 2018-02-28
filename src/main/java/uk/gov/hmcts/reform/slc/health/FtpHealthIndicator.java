package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.FtpAvailabilityChecker;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import java.time.LocalTime;

@Component
public class FtpHealthIndicator implements HealthIndicator {

    private final FtpAvailabilityChecker availabilityChecker;
    private final FtpClient client;

    public FtpHealthIndicator(
        FtpAvailabilityChecker availabilityChecker,
        FtpClient client
    ) {
        this.availabilityChecker = availabilityChecker;
        this.client = client;
    }

    @Override
    public Health health() {
        if (!availabilityChecker.isFtpAvailable(LocalTime.now())) {
            return Health.up().build();
        } else {
            try {
                client.testConnection();
                return Health.up().build();
            } catch (Exception exc) {
                return Health.down().withException(exc).build();
            }
        }
    }
}
