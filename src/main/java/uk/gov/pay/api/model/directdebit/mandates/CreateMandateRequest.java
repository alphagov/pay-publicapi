package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.pay.api.validation.ValidReturnUrl;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The structure of your request to the API when you set up a Direct Debit mandate.")
public class CreateMandateRequest {

    @NotNull
    @ValidReturnUrl
    @Size(max = 2000, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = "An HTTPS URL on your site that your user will be sent back to once they have confirmed the mandate on GOV.UK Pay.")
    private String returnUrl;

    @NotNull
    @NotBlank
    @Size(max = 255, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = "The reference number you want to associate with the mandate.")
    private String reference;

    @JsonProperty(value = "A human-readable description of the mandate.")
    @Size(min = 1, max = 255, message = "Must have a size between {min} and {max}")
    private String description;

    @ApiModelProperty(value = "mandate description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "mandate return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "mandate reference", example = "test_service_reference")
    public String getReference() {
        return reference;
    }
}
