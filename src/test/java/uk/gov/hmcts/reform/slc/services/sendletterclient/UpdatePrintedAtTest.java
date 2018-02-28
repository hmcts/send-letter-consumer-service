package uk.gov.hmcts.reform.slc.services.sendletterclient;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class UpdatePrintedAtTest {

    private static final String url = "http://localhost/";
    private final RestTemplate restTemplate = new RestTemplate();

    private MockRestServiceServer mockServer;
    private SendLetterClient client;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        client = new SendLetterClient(restTemplate, url, ZonedDateTime::now);
    }

    @Test
    public void should_send_valid_request() {
        String id = "f36e834a-216c-48ed-8fe9-b0dabc4daa49";
        String datetime = "2018-01-01T21:11:00Z";

        mockServer
            .expect(requestTo(url + id + "/printed-at"))
            .andExpect(method(PUT))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header(SendLetterClient.AUTHORIZATION_HEADER, "some-header"))
            .andExpect(content().string("{\"printed_at\":\"" + datetime + "\"}"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andRespond(withStatus(NO_CONTENT));

        client.updatePrintedAt(new LetterPrintStatus(id, ZonedDateTime.parse(datetime)));

        mockServer.verify();
    }

    @Test
    public void should_not_throw_exception_when_server_responds_with_an_error() {
        String id = "f36e834a-216c-48ed-8fe9-b0dabc4daa49";

        mockServer
            .expect(requestTo(url + id + "/printed-at"))
            .andExpect(method(PUT))
            .andRespond(withServerError());

        Throwable exception = catchThrowable(() -> client.updatePrintedAt(new LetterPrintStatus(id, now())));

        assertThat(exception).isNull();

        mockServer.verify();
    }
}
