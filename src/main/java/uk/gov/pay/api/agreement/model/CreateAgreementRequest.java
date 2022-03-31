package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.api.utils.JsonStringBuilder;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAgreementRequest {
    
    public static final int REFERENCE_MIN_LENGTH = 1;
    public static final int REFERENCE_MAX_LENGTH = 255;
    @Length(min= REFERENCE_MIN_LENGTH, max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @JsonProperty("reference")
    private String reference;

    public CreateAgreementRequest() {
        // for Jackson
    }
    
    public CreateAgreementRequest(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public String toConnectorPayload() {
        return new JsonStringBuilder()
                .add("reference", this.getReference())
                .build();
    }

    @Override
    public boolean equals(Object o) {   
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAgreementRequest that = (CreateAgreementRequest) o;
        return Objects.equals(reference, that.reference);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }
}
