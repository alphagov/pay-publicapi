package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.commons.model.directdebit.agreement.AgreementType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "CreateAgreementRequest", description = "The Payload to create a new Agreement")
public class CreateAgreementRequest {

    private static final String RETURN_URL_FIELD_NAME = "return_url";
    private static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    private static final String REFERENCE_FIELD_NAME = "reference";

    private String returnUrl;
    private AgreementType agreementType;
    private String reference;

    @ApiModelProperty(value = "agreement return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty(value = RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "agreement type", required = true, example = "ON_DEMAND")
    @JsonProperty(value = AGREEMENT_TYPE_FIELD_NAME)
    public AgreementType getAgreementType() {
        return agreementType;
    }

    @ApiModelProperty(value = "agreement reference", example = "test_service_reference")
    @JsonProperty(value = REFERENCE_FIELD_NAME)
    public String getReference() {
        return reference;
    }
}
