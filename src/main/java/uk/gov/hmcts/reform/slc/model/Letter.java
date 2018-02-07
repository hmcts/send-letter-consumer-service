package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class Letter {

    @NotEmpty
    public final List<Document> documents;

    @NotEmpty
    public final String type;

    @NotEmpty
    public final String service;

    public Letter(
        @JsonProperty("documents") List<Document> documents,
        @JsonProperty("type") String type,
        @JsonProperty("service") String service
    ) {
        this.documents = documents;
        this.type = type;
        this.service = service;
    }
}
