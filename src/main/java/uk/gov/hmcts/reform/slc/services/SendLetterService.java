package uk.gov.hmcts.reform.slc.services;

import com.microsoft.azure.servicebus.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfCreator;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.LetterMapper;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpUploader;

import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@Service
public class SendLetterService {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterService.class);

    private final LetterMapper letterMapper;
    private final PdfCreator pdfCreator;
    private final FtpUploader ftpUploader;

    public SendLetterService(
        LetterMapper letterMapper,
        PdfCreator pdfCreator,
        FtpUploader ftpUploader
    ) {
        this.letterMapper = letterMapper;
        this.pdfCreator = pdfCreator;
        this.ftpUploader = ftpUploader;
    }

    public MessageHandlingResult send(IMessage msg) {
        try {
            Letter letter = letterMapper.from(msg);
            PdfDoc pdf = pdfCreator.create(letter);
            // TODO: encrypt & sign
            ftpUploader.upload(pdf);

            return SUCCESS;

        } catch (Exception exc) {
            logger.error(exc.getMessage(), exc.getCause());

            return FAILURE;
        }
    }
}