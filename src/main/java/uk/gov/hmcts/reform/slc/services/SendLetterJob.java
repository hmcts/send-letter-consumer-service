package uk.gov.hmcts.reform.slc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageProcessor;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfCreator;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.LetterMapper;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.SftpUploader;

import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@Component
public class SendLetterJob {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterJob.class);

    private final MessageProcessor processor;
    private final LetterMapper letterMapper;
    private final PdfCreator pdfCreator;
    private final SftpUploader sftpUploader;

    public SendLetterJob(
        MessageProcessor processor,
        LetterMapper letterMapper,
        PdfCreator pdfCreator,
        SftpUploader sftpUploader
    ) {
        this.processor = processor;
        this.letterMapper = letterMapper;
        this.pdfCreator = pdfCreator;
        this.sftpUploader = sftpUploader;
    }

    @Scheduled(fixedDelayString = "${servicebus.interval}")
    public void run() {
        processor.handle(msg -> {
            try {

                Letter letter = letterMapper.from(msg);
                logger.info("Processing letter " + letter);
                PdfDoc pdf = pdfCreator.create(letter);
                // TODO: encrypt & sign
                sftpUploader.upload(pdf);

                return SUCCESS;

            } catch (Exception exc) {
                logger.error(exc.getMessage(), exc.getCause());
                return FAILURE;
            }
        });
    }
}
