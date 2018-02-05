package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class Letter {

    @NotNull
    public final String template;
    @NotNull
    public final Map<String, Object> values;
    @NotNull
    public final String type;
    @NotNull
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
