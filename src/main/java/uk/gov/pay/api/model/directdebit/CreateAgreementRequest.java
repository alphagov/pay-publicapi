package uk.gov.pay.api.model.directdebit;

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

    private String returnUrl;
    private AgreementType agreementType;

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

    @Override
    public String toString() {
        return "CreateAgreementRequest{" +
                "returnUrl='" + returnUrl + "'" +
                ", agreementType='" + agreementType + "'" +
                '}';
    }
}
