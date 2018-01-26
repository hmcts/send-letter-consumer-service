package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Letter {

    public final String template;
    public final Map<String, Object> values;
    public final String type;

    public Letter(
        @JsonProperty("template") String template,
        @JsonProperty("values") Map<String, Object> values,
        @JsonProperty("type") String type
    ) {
        this.template = template;
        this.values = values;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Letter{"
            + "template='" + template + '\''
            + ", values=" + values
            + ", type='" + type + '\''
            + '}';
    }
}
