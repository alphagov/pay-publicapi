package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;

@ApiModel(value = "GetMandateResponse")
public class GetMandateResponse {

    private static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    private static final String RETURN_URL_FIELD_NAME = "return_url";
    private static final String STATE_FIELD_NAME = "state";
    private static final String LINKS_FIELD_NAME = "_links";
    private static final String PROVIDER_ID_FIELD_NAME = "provider_id";
    private static final String REFERENCE_FIELD_NAME = "reference";

    private String agreementId;
    private String providerId;
    private String reference;
    private String returnUrl;
    private MandateStatus state;
    private MandateLinks links;

    private GetMandateResponse(String agreementId,
                               String providerId,
                               String reference,
                               String returnUrl,
                               MandateStatus state,
                               MandateLinks links) {
        this.agreementId = agreementId;
        this.providerId = providerId;
        this.reference = reference;
        this.returnUrl = returnUrl;
        this.state = state;
        this.links = links;
    }

    public static GetMandateResponse from(MandateConnectorResponse mandate, MandateLinks links) {
        return new GetMandateResponse(
                mandate.getMandateId(),
                mandate.getMandateReference(),
                mandate.getServiceReference(),
                mandate.getReturnUrl(),
                MandateStatus.valueOf(mandate.getState().getStatus().toUpperCase()),
                links);
    }

    @ApiModelProperty(value = "mandate id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = AGREEMENT_ID_FIELD_NAME)
    public String getAgreementId() {
        return this.agreementId;
    }

    @ApiModelProperty(value = "provider id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = PROVIDER_ID_FIELD_NAME)
    public String getProviderId() { return providerId; }

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty(RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "mandate state", required = true, example = "CREATED")
    @JsonProperty(value = STATE_FIELD_NAME)
    public MandateStatus getState() {
        return state;
    }

    @ApiModelProperty(value = "links", required = true)
    @JsonProperty(value = LINKS_FIELD_NAME)
    public MandateLinks getLinks() {
        return links;
    }

    @ApiModelProperty(value = "service reference", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = REFERENCE_FIELD_NAME)
    public String getReference() { return reference; }
}
