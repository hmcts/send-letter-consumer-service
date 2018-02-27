package uk.gov.hmcts.reform.slc.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.FtpAvailabilityChecker;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import java.time.LocalTime;

@Component
public class FtpHealthIndicator extends AbstractHealthIndicator {

    private final FtpAvailabilityChecker availabilityChecker;
    private final FtpClient client;

    public FtpHealthIndicator(FtpAvailabilityChecker availabilityChecker,
                              FtpClient client) {
        this.availabilityChecker = availabilityChecker;
        this.client = client;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (availabilityChecker.isFtpAvailable(LocalTime.now()) && !client.isHealthy()) {
            builder.down();
        } else {
            builder.up();
        }
    }
}
