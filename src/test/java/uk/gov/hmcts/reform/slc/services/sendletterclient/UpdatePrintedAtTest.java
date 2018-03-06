package uk.gov.hmcts.reform.slc.services.sendletterclient;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class UpdatePrintedAtTest {

    private static final UUID LETTER_ID = UUID.randomUUID();

    private static final String AUTH_HEADER = "service-auth-header";

    private static final String PRODUCER_URL = "http://localhost/";

    private static final String API_URL = PRODUCER_URL + "letters/" + LETTER_ID + "/printed-at";

    private final RestTemplate restTemplate = new RestTemplate();

    private MockRestServiceServer mockServer;
    private SendLetterClient client;

    @Before
    public void setUp() {
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        when(authTokenGenerator.generate()).thenReturn(AUTH_HEADER);

        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        client = new SendLetterClient(restTemplate, PRODUCER_URL, ZonedDateTime::now, authTokenGenerator);
    }

    @Test
    public void should_send_valid_request() {
        String datetime = "2018-01-01T21:11:00Z";

        mockServer
            .expect(requestTo(API_URL))
            .andExpect(method(PUT))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header(SendLetterClient.AUTHORIZATION_HEADER, AUTH_HEADER))
            .andExpect(content().string("{\"printed_at\":\"" + datetime + "\"}"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andRespond(withStatus(NO_CONTENT));

        client.updatePrintedAt(new LetterPrintStatus(LETTER_ID.toString(), ZonedDateTime.parse(datetime)));

        mockServer.verify();
    }

    @Test
    public void should_throw_exception_when_server_responds_with_an_error() {
        mockServer
            .expect(requestTo(API_URL))
            .andExpect(method(PUT))
            .andRespond(withServerError());

        Throwable exception = catchThrowable(() ->
            client.updatePrintedAt(new LetterPrintStatus(LETTER_ID.toString(), now()))
        );

        assertThat(exception).isNotNull();

        mockServer.verify();
    }
}
