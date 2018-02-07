package uk.gov.hmcts.reform.slc.services.steps.maptoletter;

import com.microsoft.azure.servicebus.IMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.exceptions.InvalidMessageException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class LetterMapperTest {

    private LetterMapper letterMapper;
    private IMessage message;

    @Before
    public void setUp() {
        this.letterMapper = new LetterMapper();
        this.message = mock(IMessage.class);
    }

    @Test
    public void should_return_letter_if_message_contains_valid_json() {
        given(message.getBody())
            .willReturn(
                ("{"
                    + "\"documents\": ["
                    + "  {"
                    + "    \"template\": \"whatever\","
                    + "    \"values\": { \"a\": \"b\" }"
                    + "  }"
                    + "],"
                    + "\"type\": \"some_type\","
                    + "\"service\": \"some_service\""
                    + "}"
                ).getBytes()
            );

        Letter letter = letterMapper.from(message);

        assertThat(letter).isNotNull();
        assertThat(letter.documents).hasSize(1);
        assertThat(letter.type).isEqualTo("some_type");
        assertThat(letter.service).isEqualTo("some_service");
    }

    @Test
    public void should_throw_an_exception_if_message_contains_invalid_json() {
        given(message.getBody())
            .willReturn("{\"a\" : \"b\"}".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Unable to deserialize message");
    }

    @Test
    public void should_throw_an_exception_if_message_contains_empty_json() {
        given(message.getBody()).willReturn("{}".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Invalid message body");
    }

    @Test
    public void should_throw_an_exception_if_required_field_is_empty() {
        given(message.getBody()).willReturn(
            ("{"
                + "\"documents\": ["
                + "  {"
                + "    \"template\": \"whatever\","
                + "    \"values\": { \"a\": \"b\" }"
                + "  }"
                + "],"
                + "\"type\": \"\","
                + "\"service\": \"some_service\""
                + "}"
            ).getBytes()
        );

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Invalid message body");
    }

    @Test
    public void should_throw_an_exception_if_message_is_null() {
        given(message.getBody()).willReturn("null".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Empty message");
    }
}
