package uk.gov.hmcts.reform.slc.services.steps.maptoletter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.io.IOException;

/**
 * Maps Service Bus message to a letter instance.
 */
@Component
public class LetterMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Letter from(IMessage msg) {
        try {
            Letter letter = objectMapper.readValue(msg.getBody(), Letter.class);
            if (letter != null) {
                return letter;
            } else {
                throw new InvalidMessageException("Empty message " + msg.getMessageId());
            }

        } catch (IOException exc) {
            throw new InvalidMessageException("Unable to deserialize message " + msg.getMessageId(), exc);
        }
    }
}
