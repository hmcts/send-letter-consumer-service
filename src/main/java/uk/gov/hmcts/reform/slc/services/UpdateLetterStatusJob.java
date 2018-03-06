package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.ParsedReport;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.Report;

import java.util.List;
import java.util.Objects;

import static java.time.LocalTime.now;
import static java.util.stream.Collectors.toList;

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
                .map(this::tryParse)
                .filter(Objects::nonNull)
                .forEach(parsedReport -> {
                    List<Boolean> successStatuses = parsedReport
                        .statuses
                        .stream()
                        .map(this::trySendUpdate)
                        .collect(toList());

                    if (successStatuses.stream().allMatch(s -> s == true)) {
                        ftpClient.deleteReport(parsedReport.path);
                    }
                });
        } else {
            logger.trace("FTP server not available, job cancelled");
        }
    }

    private ParsedReport tryParse(Report report) {
        try {
            return parser.parse(report);
        } catch (Exception exc) {
            logger.error("Error parsing report " + report.path, exc);
            return null;
        }
    }

    /**
     * Tries to send an update on when letter was printed.
     *
     * @return true if update succeeded.
     */
    private boolean trySendUpdate(LetterPrintStatus letterStatus) {
        try {
            sendLetterClient.updatePrintedAt(letterStatus);
            return true;
        } catch (Exception exc) {
            logger.error("Error updating status for letter: " + letterStatus.id, exc);
            return false;
        }
    }
}
