package uk.gov.hmcts.reform.slc.services.steps.maptoletter;

import com.microsoft.azure.servicebus.IMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.maptoletter.exceptions.InvalidMessageException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LetterMapperTest {

    private LetterMapper letterMapper;
    private IMessage message;

    @Mock
    private AppInsights insights;

    @Before
    public void setUp() {
        letterMapper = new LetterMapper();
        message = mock(IMessage.class);

        ReflectionTestUtils.setField(letterMapper, "insights", insights);
    }

    @Test
    public void should_return_letter_if_message_contains_valid_json() {
        given(message.getBody())
            .willReturn(
                ("{"
                    + "\"id\": \"9903ED01-A73C-4874-8ABF-D2678E3AE23D\","
                    + "\"documents\": ["
                    + "  {"
                    + "    \"template\": \"whatever\","
                    + "    \"values\": { \"a\": \"b\" }"
                    + "  }"
                    + "],"
                    + "\"type\": \"some_type\","
                    + "\"service\": \"some_service\","
                    + "\"message_id\": \"syf8f7\","
                    + "\"additional_data\": {"
                    + "  \"document_type\": \"claim\""
                    + "}"
                    + "}"
                ).getBytes()
            );

        Letter letter = letterMapper.from(message);

        assertThat(letter).isNotNull();
        assertThat(letter.documents).hasSize(1);
        assertThat(letter.type).isEqualTo("some_type");
        assertThat(letter.service).isEqualTo("some_service");

        verify(insights).trackMessageMappedToLetter(anyString(), eq("some_service"), eq("whatever"), anyLong());
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_throw_an_exception_if_message_contains_invalid_json() {
        given(message.getBody())
            .willReturn("{\"a\" : \"b\"}".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Unable to deserialize message");

        verify(insights).trackMessageNotMapped(anyString(), anyLong());
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_throw_an_exception_if_message_contains_empty_json() {
        given(message.getBody()).willReturn("{}".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Invalid message body");

        verify(insights).trackMessageMappedToInvalid(anyString(), anyLong());
        verifyNoMoreInteractions(insights);
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

        verify(insights).trackMessageMappedToInvalid(anyString(), anyLong());
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_throw_an_exception_if_message_is_null() {
        given(message.getBody()).willReturn("null".getBytes());

        assertThatThrownBy(() -> letterMapper.from(message))
            .isInstanceOf(InvalidMessageException.class)
            .hasMessageStartingWith("Empty message");

        verify(insights).trackMessageMappedToNull(anyString());
        verifyNoMoreInteractions(insights);
    }
}
