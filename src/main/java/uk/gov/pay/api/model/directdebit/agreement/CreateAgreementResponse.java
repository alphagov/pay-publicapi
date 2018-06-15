package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateAgreementResponse", description = "The Agreement Payload to create a new Agreement")
public class CreateAgreementResponse {

    public static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    public static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String CREATED_DATE_FIELD_NAME = "created_date";
    public static final String STATE_FIELD_NAME = "state";

    private String agreementId;
    private AgreementType agreementType;
    private String returnUrl;
    private String createdDate;
    private AgreementStatus state;

    public CreateAgreementResponse(String agreementId, AgreementType agreementType, String returnUrl, String createdDate, AgreementStatus state) {
        this.agreementId = agreementId;
        this.agreementType = agreementType;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
    }

    @ApiModelProperty(value = "agreement id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = AGREEMENT_ID_FIELD_NAME)
    public String getAgreementId() {
        return this.agreementId;
    }

    @ApiModelProperty(value = "agreement type", required = true, example = "ON_DEMAND")
    @JsonProperty(value = AGREEMENT_TYPE_FIELD_NAME)
    public AgreementType getAgreementType() {
        return this.agreementType;
    }

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty(RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "agreement created date", required = true)
    @JsonProperty(CREATED_DATE_FIELD_NAME)
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(value = "agreement state", required = true, example = "CREATED")
    @JsonProperty(value = STATE_FIELD_NAME)
    public AgreementStatus getState() {
        return state;
    }

    @Override
    public String toString() {
        return "CreateAgreementResponse{" +
                "agreementId='" + agreementId + "'" +
                ", agreementType='" + agreementType + "'" +
                ", returnUrl='" + returnUrl + "'" +
                ", createdDate='" + createdDate + "'" +
                ", state=" + state + "'" +
                '}';
    }
}
