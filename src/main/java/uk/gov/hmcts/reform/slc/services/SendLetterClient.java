package uk.gov.hmcts.reform.slc.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;

@Component
public class SendLetterClient {

    private static final Logger logger = LoggerFactory.getLogger(SendLetterService.class);

    private final RestTemplate restTemplate;
    private final String sendLetterProducerUrl;
    private final Supplier<ZonedDateTime> currentDateTimeSupplier;

    public SendLetterClient(
        RestTemplate restTemplate,
        @Value("${sendletter.producer.url}") String sendLetterProducerUrl,
        Supplier<ZonedDateTime> currentDateTimeSupplier
    ) {
        this.restTemplate = restTemplate;
        this.sendLetterProducerUrl = appendIfMissing(sendLetterProducerUrl, "/");
        this.currentDateTimeSupplier = currentDateTimeSupplier;
    }

    public void updateSentToPrintAt(UUID letterId) {
        try {
            restTemplate.put(
                sendLetterProducerUrl + letterId + "/sent-to-print-at",
                ImmutableMap.of(
                    "sent_to_print_at",
                    currentDateTimeSupplier.get().format(ISO_INSTANT)
                )
            );
        } catch (RestClientException exception) {
            //If updating timestamp fails just log the message as the letter is already uploaded
            logger.error(
                "Exception occurred while updating sent to print time for letter id = " + letterId,
                exception
            );
        }
    }

    public void updatePrintedAt(LetterPrintStatus status) {
        try {
            restTemplate.put(
                sendLetterProducerUrl + status.id + "/printed-at",
                ImmutableMap.of(
                    "printed_at",
                    status.printedAt.format(ISO_INSTANT)
                )
            );
        } catch (RestClientException exception) {
            logger.error("Exception occurred while updating printed_at time for letter id = " + status.id, exception);
        }
    }

    public void updateIsFailedStatus(UUID letterId) {
        try {
            restTemplate.put(sendLetterProducerUrl + letterId + "/is-failed", null);
        } catch (RestClientException exception) {
            logger.error(
                "Exception occurred while updating is failed status for letter id = " + letterId,
                exception
            );
        }
    }
}
