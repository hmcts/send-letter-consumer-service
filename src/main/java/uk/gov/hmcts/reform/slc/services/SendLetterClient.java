package uk.gov.hmcts.reform.slc.services;

import com.google.common.collect.ImmutableMap;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class SendLetterClient {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterService.class);

    private final RestTemplate restTemplate;
    private final String sendLetterProducerUrl;
    private final Supplier<ZonedDateTime> supplier;

    public SendLetterClient(
        RestTemplate restTemplate,
        @Value("${sendletter.producer.url}") String sendLetterProducerUrl,
        Supplier<ZonedDateTime> supplier
    ) {
        this.restTemplate = restTemplate;
        this.sendLetterProducerUrl = sendLetterProducerUrl;
        this.supplier = supplier;
    }

    public void updateSentToPrintAt(UUID letterId) {
        try {
            restTemplate.put(normalizedUrl(letterId),
                ImmutableMap.of(
                    "sent_to_print_at",
                    supplier.get().format(DateTimeFormatter.ISO_INSTANT)));
        } catch (URISyntaxException | RestClientException exception) {
            //If updating timestamp fails just log the message as the letter is already uploaded
            logger.error(
                "Exception occurred while updating sent to print time for letter id = {}",
                letterId,
                exception
            );
        }
    }

    private String normalizedUrl(UUID letterId) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(sendLetterProducerUrl);

        return uriBuilder.setPath(uriBuilder.getPath() + letterId + "/sent-to-print-at")
            .build()
            .normalize()
            .toString();
    }
}
