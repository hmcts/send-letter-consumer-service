package uk.gov.hmcts.reform.slc.services;

import com.microsoft.azure.servicebus.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfCreator;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.LetterMapper;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;
import uk.gov.hmcts.reform.slc.services.steps.zip.ZipFileNameHelper;
import uk.gov.hmcts.reform.slc.services.steps.zip.ZippedDoc;
import uk.gov.hmcts.reform.slc.services.steps.zip.Zipper;

import java.util.Objects;

import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@Service
public class SendLetterService {
    public static final String SMOKE_TEST_LETTER_TYPE = "smoke_test";

    private static final Logger logger = LoggerFactory.getLogger(SendLetterService.class);

    private final LetterMapper letterMapper;
    private final PdfCreator pdfCreator;
    private final Zipper zipper;
    private final FtpClient ftpClient;
    private final SendLetterClient sendLetterClient;
    private final AppInsights insights;

    public SendLetterService(
        LetterMapper letterMapper,
        PdfCreator pdfCreator,
        Zipper zipper,
        FtpClient ftpClient,
        SendLetterClient sendLetterClient,
        AppInsights insights
    ) {
        this.letterMapper = letterMapper;
        this.pdfCreator = pdfCreator;
        this.zipper = zipper;
        this.ftpClient = ftpClient;
        this.sendLetterClient = sendLetterClient;
        this.insights = insights;
    }

    public MessageHandlingResult send(IMessage msg) {
        Letter letter = null;

        try {
            letter = letterMapper.from(msg);
            PdfDoc pdf = pdfCreator.create(letter);
            // TODO: encrypt & sign
            ZippedDoc zippedDoc = zipper.zip(ZipFileNameHelper.generateName(letter), pdf);
            ftpClient.upload(zippedDoc, isSmokeTest(letter));

            //update producer with sent to print at time for reporting
            sendLetterClient.updateSentToPrintAt(letter.id);

            return SUCCESS;

        } catch (Exception exc) {
            logger.error("Exception occurred while processing message", exc);

            //update producer with is_failed status for reporting
            if (Objects.nonNull(letter)) {
                insights.trackLetterNotHandled(letter);
                sendLetterClient.updateIsFailedStatus(letter.id);
            } else {
                logger.error("Unable to update is_failed status in producer for reporting");
            }

            return FAILURE;
        }
    }

    private boolean isSmokeTest(Letter letter) {
        return Objects.equals(letter.type, SMOKE_TEST_LETTER_TYPE);
    }
}
