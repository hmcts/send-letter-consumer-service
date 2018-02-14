package uk.gov.hmcts.reform.slc.services;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

;

public class SendLetterClientTest {

    private SendLetterClient sendLetterClient;

    private MockRestServiceServer mockServer;

    private RestTemplate restTemplate = new RestTemplate();

    private String letterId = UUID.randomUUID().toString();

    private static final String SENT_TO_PRINT_AT = "/sent-to-print-at";

    private static final String sendLetterProducerUrl = "http://localhost:5432/";

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        sendLetterClient = new SendLetterClient(restTemplate, sendLetterProducerUrl);
    }

    @Test
    public void should_successfully_put_sent_to_print_at_attribute_to_letter_service() {
        //given
        mockServer.expect(requestTo(sendLetterProducerUrl + letterId + SENT_TO_PRINT_AT))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));

        //when
        sendLetterClient.updateSentToPrintAt(letterId);

        //then
        mockServer.verify();
    }

    @Test
    public void should_fail_to_put_sent_to_print_at_attribute_to_letter_service() {
        //given
        mockServer.expect(requestTo(sendLetterProducerUrl + letterId + SENT_TO_PRINT_AT))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withServerError());

        //when
        sendLetterClient.updateSentToPrintAt(letterId);

        //then
        mockServer.verify();
    }
}
