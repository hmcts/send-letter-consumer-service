package uk.gov.hmcts.reform.slc.services.sendletterclient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.slc.services.SendLetterClient;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ServiceHealthyTest {

    private static final String url = "http://localhost/";
    private final RestTemplate restTemplate = new RestTemplate();

    private MockRestServiceServer mockServer;
    private SendLetterClient client;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        client = new SendLetterClient(restTemplate, url, ZonedDateTime::now, mock(AuthTokenGenerator.class));
    }

    @Test
    public void service_should_be_healthy() {
        setUpMockServer(UP);

        assertThat(client.serviceHealthy().getStatus()).isEqualTo(UP);

        mockServer.verify();
    }

    @Test
    public void should_not_throw_exception_when_server_responds_with_an_error() {
        setUpMockServer(DOWN);

        assertThat(client.serviceHealthy().getStatus()).isEqualTo(DOWN);

        mockServer.verify();
    }

    private void setUpMockServer(Status status) {
        mockServer
            .expect(requestTo(url + "health"))
            .andExpect(method(GET))
            .andRespond(withSuccess(
                "{\"status\":\"" + status.toString() + "\"}",
                MediaType.APPLICATION_JSON
            ));
    }
}
