package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.CreateAgreementRequestBuilder;
import uk.gov.pay.api.utils.JsonStringBuilder;
import javax.validation.constraints.Size;
import java.util.Objects;

@Schema(description = "The Agreement Request Payload")
public class CreateAgreementRequest {
    
    public static final int REFERENCE_MIN_LENGTH = 1;
    public static final int REFERENCE_MAX_LENGTH = 255;

    @JsonProperty("reference")
    @Size(min= REFERENCE_MIN_LENGTH, max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;

    public CreateAgreementRequest() {
        // for Jackson
    }
    
    public CreateAgreementRequest(CreateAgreementRequestBuilder builder) {
        this.reference = builder.getReference();
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
