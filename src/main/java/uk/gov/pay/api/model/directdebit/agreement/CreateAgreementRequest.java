package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "CreateAgreementRequest", description = "The Payload to create a new Agreement")
public class CreateAgreementRequest {

    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    public static final String REFERENCE_FIELD_NAME = "reference";

    private String returnUrl;
    private AgreementType agreementType;
    private String reference;

    // required for Jackson
    private CreateAgreementRequest() {
    }

    public CreateAgreementRequest(String returnUrl, AgreementType agreementType) {
        this(returnUrl, agreementType, null);
    }

    public CreateAgreementRequest(String returnUrl, AgreementType agreementType, String reference) {
        this.returnUrl = returnUrl;
        this.agreementType = agreementType;
        this.reference = reference;
    }

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

    @ApiModelProperty(value = "agreement reference", example = "serviceref123")
    @JsonProperty(value = REFERENCE_FIELD_NAME)
    public String getReference() {
        return reference;
    }
}
