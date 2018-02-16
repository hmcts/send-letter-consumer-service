package uk.gov.hmcts.reform.slc.services;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class SendLetterClientTest {

    private MockRestServiceServer mockServer;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final UUID letterId = UUID.randomUUID();

    private static final String SENT_TO_PRINT_AT = "/sent-to-print-at";

    private static final String sendLetterProducerUrl = "http://localhost:5432/";

    private static final ZonedDateTime now = ZonedDateTime.now();

    private String isoDate;

    @Before
    public void setUp() {
        isoDate = now.format(DateTimeFormatter.ISO_INSTANT);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void should_successfully_put_sent_to_print_at_attribute_when_base_url_contains_slash_suffixed() {
        //given
        SendLetterClient sendLetterClient = new SendLetterClient(restTemplate, sendLetterProducerUrl, () -> now);

        mockServer.expect(requestTo(sendLetterProducerUrl + letterId + SENT_TO_PRINT_AT))
            .andExpect(content().string("{\"sent_to_print_at\":\"" + isoDate + "\"}"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));

        //when
        sendLetterClient.updateSentToPrintAt(letterId);

        //then
        mockServer.verify();
    }

    @Test
    public void should_successfully_put_sent_to_print_at_attribute_when_base_url_contains_no_slash_suffixed() {
        //given
        SendLetterClient sendLetterClient = new SendLetterClient(
            restTemplate,
            removeEnd(sendLetterProducerUrl, "/"),
            () -> now
        );

        mockServer.expect(requestTo(sendLetterProducerUrl + letterId + SENT_TO_PRINT_AT))
            .andExpect(content().string("{\"sent_to_print_at\":\"" + isoDate + "\"}"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));

        //when
        sendLetterClient.updateSentToPrintAt(letterId);

        //then
        mockServer.verify();
    }

    @Test
    public void should_not_throw_exception_when_rest_template_throw_server_error() {
        //given
        SendLetterClient sendLetterClient = new SendLetterClient(restTemplate, sendLetterProducerUrl, () -> now);

        mockServer.expect(requestTo(sendLetterProducerUrl + letterId + SENT_TO_PRINT_AT))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withServerError());

        //when
        Throwable exception = catchThrowable(() -> {
            sendLetterClient.updateSentToPrintAt(letterId);
        });

        //then
        assertThat(exception).isNull();

        mockServer.verify();
    }
}
