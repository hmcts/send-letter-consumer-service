package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Letter {

    public final String template;
    public final Map<String, Object> values;
    public final String type;
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
