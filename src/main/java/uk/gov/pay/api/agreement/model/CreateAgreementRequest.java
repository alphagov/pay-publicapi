package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.CreateAgreementRequestBuilder;
import uk.gov.pay.api.utils.JsonStringBuilder;
import javax.validation.constraints.Size;
import java.util.Objects;

@Schema(description = "The AgreementLedgerResponse Request Payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAgreementRequest {
    
    public static final String USER_IDENTIFIER_FIELD = "user_identifier";

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 255;

    @JsonProperty("reference")
    @Size(min= MIN_LENGTH, max = MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;

    @JsonProperty("description")
    @Size(min= MIN_LENGTH, max = MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String description;

    @JsonProperty(USER_IDENTIFIER_FIELD)
    @Size(min= MIN_LENGTH, max = MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String userIdentifier;

    public CreateAgreementRequest() {
        // for Jackson
    }
    
    public CreateAgreementRequest(CreateAgreementRequestBuilder builder) {
        this.reference = builder.getReference();
        this.description = builder.getDescription();
        this.userIdentifier = builder.getUserIdentifier();
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String toConnectorPayload() {
        var stringBuilder = new JsonStringBuilder()
                .add("reference", this.getReference())
                .add("description", this.getDescription());

        if (this.getUserIdentifier() != null) {
            stringBuilder.add("user_identifier", this.getUserIdentifier());
        }
        return stringBuilder.build();
    }

    @Override
    public boolean equals(Object o) {   
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAgreementRequest that = (CreateAgreementRequest) o;
        return Objects.equals(reference, that.reference) && Objects.equals(description, that.description) && Objects.equals(userIdentifier, that.userIdentifier);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reference, description, userIdentifier);
    }
}
