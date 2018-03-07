package uk.gov.hmcts.reform.slc.services;

import com.microsoft.azure.servicebus.IMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;
import uk.gov.hmcts.reform.slc.model.Document;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfCreator;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.LetterMapper;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.FtpClient;

import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.FAILURE;
import static uk.gov.hmcts.reform.slc.services.servicebus.MessageHandlingResult.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class SendLetterServiceTest {

    @Mock private LetterMapper letterMapper;
    @Mock private PdfCreator pdfCreator;
    @Mock private FtpClient ftpClient;
    @Mock private SendLetterClient sendLetterClient;

    @Mock private IMessage message;

    private SendLetterService service;

    @Before
    public void setUp() throws Exception {
        service = new SendLetterService(letterMapper, pdfCreator, ftpClient, sendLetterClient);
    }

    @Test
    public void should_return_success_status_if_all_operations_succeeded() {
        given(letterMapper.from(any()))
            .willReturn(sampleLetter());
        given(pdfCreator.create(any()))
            .willReturn(new PdfDoc("hello.pdf", "hello".getBytes()));

        // when
        MessageHandlingResult result = service.send(message);

        // then
        assertThat(result).isEqualTo(SUCCESS);
    }

    @Test
    public void should_return_failure_status_if_any_of_the_operations_failed() {
        given(letterMapper.from(any()))
            .willReturn(sampleLetter());
        given(pdfCreator.create(any()))
            .willThrow(PDFServiceClientException.class);

        // when
        MessageHandlingResult result = service.send(message);

        // then
        assertThat(result).isEqualTo(FAILURE);
    }

    @Test
    public void should_handle_smoke_test_letters() {

        IMessage msg1 = mock(IMessage.class);
        IMessage msg2 = mock(IMessage.class);


        Letter smokeTestLetter = new Letter(
            UUID.randomUUID(),
            singletonList(
                new Document("template", emptyMap())
            ),
            "smoke_test",
            "cmc"
        );

        given(letterMapper.from(msg1)).willReturn(smokeTestLetter);
        given(letterMapper.from(msg2)).willReturn(sampleLetter());

        service.send(msg1);
        verify(ftpClient).upload(any(), eq(true));

        service.send(msg2);
        verify(ftpClient).upload(any(), eq(false));
    }

    private Letter sampleLetter() {
        return new Letter(
            UUID.randomUUID(),
            singletonList(
                new Document("template", emptyMap())
            ),
            "some_type",
            "cmc"
        );
    }
}
