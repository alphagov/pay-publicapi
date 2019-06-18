package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "CreateMandateRequest", description = "The Payload to create a new Agreement")
public class CreateMandateRequest {

    @NotNull
    @JsonProperty(value = "return_url")
    private String returnUrl;

    @NotNull
    @JsonProperty(value = "reference")
    private String reference;
    
    @JsonProperty(value = "description")
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
