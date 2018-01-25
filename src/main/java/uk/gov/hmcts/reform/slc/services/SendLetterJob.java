package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageProcessor;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.LetterMapper;

@Component
public class SendLetterJob {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterJob.class);

    private final MessageProcessor processor;
    private final LetterMapper letterMapper;

    public SendLetterJob(
        MessageProcessor processor,
        LetterMapper letterMapper
    ) {
        this.processor = processor;
        this.letterMapper = letterMapper;
    }

    @Scheduled(fixedDelay = 30_000)
    public void run() {
        processor.handle(msg -> {
            try {
                Letter letter = letterMapper.from(msg);
                logger.info("Processing letter " + letter);
                // TODO: generate PDF
                // TODO: send PDF to Xerox
                return MessageHandlingResult.SUCCESS;
            } catch (Exception exc) {
                logger.error(exc.getMessage(), exc.getCause());
                return MessageHandlingResult.FAILURE;
            }
        });
    }
}
