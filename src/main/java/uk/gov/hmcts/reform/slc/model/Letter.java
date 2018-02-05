package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

public class Letter {

    @NotEmpty
    public final String template;

    @NotEmpty
    public final Map<String, Object> values;

    @NotEmpty
    public final String type;

    @NotEmpty
    public final String service;

    public Letter(
        @JsonProperty("template") String template,
        @JsonProperty("values") Map<String, Object> values,
        @JsonProperty("type") String type,
        @JsonProperty("service") String service
    ) {
        this.template = template;
        this.values = values;
        this.type = type;
        this.service = service;
    }
}
