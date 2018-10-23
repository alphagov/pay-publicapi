package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.AgreementLinks;
import uk.gov.pay.commons.model.directdebit.agreement.AgreementType;

@ApiModel(value = "CreateAgreementResponse", description = "The Agreement Payload to create a new Agreement")
public class GetAgreementResponse {

    private static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    private static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    private static final String RETURN_URL_FIELD_NAME = "return_url";
    private static final String STATE_FIELD_NAME = "state";
    private static final String LINKS_FIELD_NAME = "_links";
    private static final String PROVIDER_ID_FIELD_NAME = "provider_id";
    private static final String REFERENCE_FIELD_NAME = "reference";

    private String agreementId;
    private AgreementType agreementType;
    private String providerId;
    private String reference;
    private String returnUrl;
    private AgreementStatus state;
    private AgreementLinks links;

    private GetAgreementResponse(String agreementId,
                                 AgreementType agreementType,
                                 String providerId, String reference, String returnUrl,
                                 AgreementStatus state,
                                 AgreementLinks links) {
        this.agreementId = agreementId;
        this.agreementType = agreementType;
        this.providerId = providerId;
        this.reference = reference;
        this.returnUrl = returnUrl;
        this.state = state;
        this.links = links;
    }

    public static GetAgreementResponse from(MandateConnectorResponse mandate, AgreementLinks links) {
        return new GetAgreementResponse(
                mandate.getMandateId(),
                AgreementType.valueOf(mandate.getMandateType().toString()),
                mandate.getMandateReference(),
                mandate.getServiceReference(),
                mandate.getReturnUrl(),
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

    @ApiModelProperty(value = "provider id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = PROVIDER_ID_FIELD_NAME)
    public String getProviderId() { return providerId; }

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty(RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
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

    @ApiModelProperty(value = "service reference", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = REFERENCE_FIELD_NAME)
    public String getReference() { return reference; }
}
