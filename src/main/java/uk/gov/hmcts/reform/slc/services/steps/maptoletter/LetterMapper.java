package uk.gov.hmcts.reform.slc.services.steps.maptoletter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.exceptions.InvalidMessageException;

import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Maps Service Bus message to a letter instance.
 */
@Component
public class LetterMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public Letter from(IMessage msg) {
        try {
            return validate(objectMapper.readValue(msg.getBody(), Letter.class));
        } catch (IOException exc) {
            throw new InvalidMessageException("Unable to deserialize message " + msg.getMessageId(), exc);
        }
    }

    private Letter validate(Letter letter) throws InvalidMessageException {
        if (letter == null) {
            throw new InvalidMessageException("Invalid message body");
        }

        Set<ConstraintViolation<Letter>> violations = validator.validate(letter);

        if (!violations.isEmpty()) {
            // can work on building message from violations
            throw new InvalidMessageException("Invalid message body");
        }

        return letter;
    }
}
