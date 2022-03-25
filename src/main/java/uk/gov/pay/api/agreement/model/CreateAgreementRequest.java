package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.api.utils.JsonStringBuilder;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAgreementRequest {

    @NotNull(message = "Field [reference] cannot be null")
    @Length(min = 1, max = 255, message = "Field [reference] can have a size between 0 and 255")
    @JsonProperty("reference")
    private String reference;

    public CreateAgreementRequest() {
        // for Jackson
    }
    //TODO
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
