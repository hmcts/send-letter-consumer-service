package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import static java.time.LocalTime.now;

@Component
public class UpdateLetterStatusJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLetterStatusJob.class);

    private final FtpClient ftpClient;
    private final FtpAvailabilityChecker ftpAvailabilityChecker;

    public UpdateLetterStatusJob(FtpClient ftpClient, FtpAvailabilityChecker ftpAvailabilityChecker) {
        this.ftpClient = ftpClient;
        this.ftpAvailabilityChecker = ftpAvailabilityChecker;
    }

    @Scheduled(cron = "${reports-cron}")
    public void run() {
        logger.trace("Running job");

        if (ftpAvailabilityChecker.isFtpAvailable(now())) {
            ftpClient.downloadReports();
            // TODO: extract letter status
            // TODO: send update request to letters service
        } else {
            logger.trace("FTP server not available, job cancelled");
        }
    }
}
