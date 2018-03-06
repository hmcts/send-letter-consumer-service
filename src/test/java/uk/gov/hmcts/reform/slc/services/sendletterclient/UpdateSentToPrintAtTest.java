package uk.gov.hmcts.reform.slc.services.sendletterclient;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class UpdateSentToPrintAtTest {

    private MockRestServiceServer mockServer;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final UUID letterId = UUID.randomUUID();

    private static final String sendLetterProducerUrl = "http://localhost:5432/";

    private static final String API_URL = sendLetterProducerUrl + "letters/" + letterId + "/sent-to-print-at";

    private static final String AUTH_HEADER = "service-auth-header";

    private static final ZonedDateTime now = ZonedDateTime.now();

    private String isoDate;

    private SendLetterClient sendLetterClient;

    @Before
    public void setUp() {
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        when(authTokenGenerator.generate()).thenReturn(AUTH_HEADER);

        isoDate = now.format(DateTimeFormatter.ISO_INSTANT);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        sendLetterClient = new SendLetterClient(restTemplate, sendLetterProducerUrl, () -> now, authTokenGenerator);
    }

    @Test
    public void should_successfully_put_sent_to_print_at_attribute() {
        //given
        mockServer.expect(requestTo(API_URL))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header(SendLetterClient.AUTHORIZATION_HEADER, AUTH_HEADER))
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
        mockServer.expect(requestTo(API_URL))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withServerError());

        //when
        Throwable exception = catchThrowable(() -> sendLetterClient.updateSentToPrintAt(letterId));

        //then
        assertThat(exception).isNull();

        mockServer.verify();
    }
}
