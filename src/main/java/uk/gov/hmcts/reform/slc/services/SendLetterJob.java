package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageProcessor;

@Component
public class SendLetterJob {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterJob.class);

    private final MessageProcessor processor;

    public SendLetterJob(MessageProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${servicebus.interval}")
    public void run() {
        logger.trace("Running job");
        processor.process();
    }
}
