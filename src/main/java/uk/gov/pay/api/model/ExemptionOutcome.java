package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Outcome", description = "A structure representing the outcome of a 3DS exemption, if known.")
public class ExemptionOutcome {
    @JsonProperty("result")
    private String result;

    public ExemptionOutcome() {

    }
    public ExemptionOutcome(String result) {
        this.result = result;
    }

    @Schema(description = "The outcome of the requested exemption", example = "honoured", accessMode = READ_ONLY)
    public String getResult() {
        return result;
    }
}
