package uk.gov.hmcts.reform.slc.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;


@Component
public class SendLetterClient {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterService.class);

    private final RestTemplate restTemplate;
    private final String sendLetterProducerUrl;

    public SendLetterClient(
        RestTemplate restTemplate,
        @Value("${sendletter.producer.url}") String sendLetterProducerUrl) {
        this.restTemplate = restTemplate;
        this.sendLetterProducerUrl = sendLetterProducerUrl;
    }

    public void updateSentToPrintAt(String letterId) {
        try {
            restTemplate.put(sendLetterProducerUrl + letterId + "/sent-to-print-at",
                ImmutableMap.of(
                    "sent_to_print_at",
                    Instant.now()));
        } catch (RestClientException restException) {
            //If updating timestamp fails just log the message as the letter is already uploaded
            logger.error("Exception occurred while updating sent to print time for letter id = {}", letterId);
        }
    }
}
