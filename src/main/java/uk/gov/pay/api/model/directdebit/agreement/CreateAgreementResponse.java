package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.AgreementLinks;

@ApiModel(value = "CreateAgreementResponse", description = "The Agreement Payload to create a new Agreement")
public class CreateAgreementResponse {

    public static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    public static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String CREATED_DATE_FIELD_NAME = "created_date";
    public static final String STATE_FIELD_NAME = "state";
    public static final String LINKS_FIELD_NAME = "_links";

    private String agreementId;
    private AgreementType agreementType;
    private String returnUrl;
    private String createdDate;
    private AgreementStatus state;
    private AgreementLinks links = new AgreementLinks();

    private CreateAgreementResponse(String agreementId,
                                    AgreementType agreementType,
                                    String returnUrl,
                                    String createdDate,
                                    AgreementStatus state,
                                    AgreementLinks links) {
        this.agreementId = agreementId;
        this.agreementType = agreementType;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
        this.links = links;
    }

    public static CreateAgreementResponse from(MandateConnectorResponse mandate) {
        AgreementLinks links = new AgreementLinks();
        links.addKnownLinksValueOf(mandate.getLinks());

        return new CreateAgreementResponse(
                mandate.getMandateId(),
                AgreementType.valueOf(mandate.getMandateType()),
                mandate.getReturnUrl(),
                mandate.getCreatedDate(),
                AgreementStatus.valueOf(mandate.getState().getStatus().toUpperCase()),
                links);
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

    @ApiModelProperty(value = "links", required = true)
    @JsonProperty(value = LINKS_FIELD_NAME)
    public AgreementLinks getLinks() {
        return links;
    }
}
