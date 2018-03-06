package uk.gov.hmcts.reform.slc.services.sendletterclient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class CheckPrintStatusTest {

    private MockRestServiceServer mockServer;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PRODUCER_URL = "http://localhost:5432/";

    private static final String API_URL = PRODUCER_URL + "letter-reports/print-status-check";

    private static final String AUTH_HEADER = "service-auth-header";

    private static final ZonedDateTime now = ZonedDateTime.now();

    private SendLetterClient sendLetterClient;

    @Before
    public void setUp() {
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        when(authTokenGenerator.generate()).thenReturn(AUTH_HEADER);

        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        sendLetterClient = new SendLetterClient(restTemplate, PRODUCER_URL, () -> now, authTokenGenerator);
    }

    @Test
    public void should_successfully_post_print_status_check() {
        //given
        mockServer.expect(requestTo(API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(SendLetterClient.AUTHORIZATION_HEADER, AUTH_HEADER))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        //when
        sendLetterClient.checkPrintStatus();

        //then
        mockServer.verify();
    }

    @Test
    public void should_not_throw_exception_when_rest_template_throws_server_error() {
        //given
        mockServer.expect(requestTo(API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        //when
        Throwable exception = catchThrowable(() -> sendLetterClient.checkPrintStatus());

        //then
        assertThat(exception).isNull();

        mockServer.verify();
    }
}
