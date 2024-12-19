package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Exemption", description = "A structure representing that 3DS exemption was requested and the outcome of the exemption, if applicable.")
public class Exemption {
    @JsonProperty("requested")
    private boolean requested;
    @JsonProperty("type")
    private String type;
    @JsonProperty("outcome")
    private ExemptionOutcome outcome;

    public Exemption() {
    }

    public Exemption(Boolean requested, String type, ExemptionOutcome outcome) {
        this.requested = requested;
        this.type = type;
        this.outcome = outcome;
    }

    @Schema(description = "Indicates whether an exemption was requested for the given payment.", example = "true", accessMode = READ_ONLY)
    public boolean getRequested() {
        return requested;
    }

    @Schema(description = "Indicates the type of exemption. Only present for corporate exemption", example = "corporate", accessMode = READ_ONLY)
    public String getType() {
        return type;
    }

    @Schema(description = "The outcome of the requested exemption", accessMode = READ_ONLY)
    public ExemptionOutcome getOutcome() {
        return outcome;
    }
}
