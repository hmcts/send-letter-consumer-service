package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import java.util.Objects;

import static java.time.LocalTime.now;

@Component
public class UpdateLetterStatusJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLetterStatusJob.class);

    private final FtpClient ftpClient;
    private final FtpAvailabilityChecker ftpAvailabilityChecker;
    private final SendLetterClient sendLetterClient;
    private final ReportParser parser;


    public UpdateLetterStatusJob(
        FtpClient ftpClient,
        FtpAvailabilityChecker ftpAvailabilityChecker,
        SendLetterClient sendLetterClient,
        ReportParser parser
    ) {
        this.ftpClient = ftpClient;
        this.ftpAvailabilityChecker = ftpAvailabilityChecker;
        this.sendLetterClient = sendLetterClient;
        this.parser = parser;
    }

    @Scheduled(cron = "${ftp.reports-cron}")
    public void run() {
        logger.trace("Running job");

        if (ftpAvailabilityChecker.isFtpAvailable(now())) {
            ftpClient
                .downloadReports()
                .stream()
                .map(report -> {
                    try {
                        return parser.parse(report);
                    } catch (Exception exc) {
                        logger.error("Error parsing report " + report.path, exc);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(parsedReport -> {
                    parsedReport.statuses.forEach(sendLetterClient::updatePrintedAt);
                    ftpClient.deleteReport(parsedReport.path);
                });
        } else {
            logger.trace("FTP server not available, job cancelled");
        }
    }
}
