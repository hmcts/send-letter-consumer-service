package uk.gov.hmcts.reform.slc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public class Letter {

    @NotEmpty
    public final String id;

    @NotEmpty
    public final List<Document> documents;

    @NotEmpty
    public final String type;

    @NotEmpty
    public final String service;

    @NotEmpty
    public final String messageId;

    public final Map<String, Object> additionalData;

    public Letter(
        @JsonProperty("id") String id,
        @JsonProperty("documents") List<Document> documents,
        @JsonProperty("type") String type,
        @JsonProperty("service") String service,
        @JsonProperty("message_id") String messageId,
        @JsonProperty("additional_data") Map<String, Object> additionalData
    ) {
        this.id = id;
        this.documents = documents;
        this.type = type;
        this.service = service;
        this.messageId = messageId;
        this.additionalData = additionalData;
    }
}
