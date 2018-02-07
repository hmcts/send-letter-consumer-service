package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageProcessor;

import static java.time.LocalTime.now;

@Component
public class SendLetterJob {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterJob.class);

    private final MessageProcessor processor;
    private final FtpAvailabilityChecker ftpAvailabilityChecker;

    public SendLetterJob(
        MessageProcessor processor,
        FtpAvailabilityChecker ftpAvailabilityChecker
    ) {
        this.processor = processor;
        this.ftpAvailabilityChecker = ftpAvailabilityChecker;
    }

    @Scheduled(fixedDelayString = "${servicebus.interval}")
    public void run() {
        logger.trace("Running job");

        if (ftpAvailabilityChecker.isFtpAvailable(now())) {
            processor.process();
        } else {
            logger.trace("FTP server not available, job cancelled");
        }
    }
}
