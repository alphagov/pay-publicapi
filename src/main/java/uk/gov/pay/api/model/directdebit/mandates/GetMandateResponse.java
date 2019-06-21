package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;

@ApiModel(value = "GetMandateResponse")
public class GetMandateResponse {
    
    private final String agreementId;
    private final String providerId;
    private final String reference;
    private final String returnUrl;
    private final String mandateReference;
    private final MandateStatus state;
    private final MandateLinks links;

    private GetMandateResponse(String agreementId,
                               String providerId,
                               String reference,
                               String returnUrl,
                               String mandateReference, 
                               MandateStatus state,
                               MandateLinks links) {
        this.agreementId = agreementId;
        this.providerId = providerId;
        this.reference = reference;
        this.returnUrl = returnUrl;
        this.mandateReference = mandateReference;
        this.state = state;
        this.links = links;
    }

    public static GetMandateResponse from(MandateConnectorResponse mandate, MandateLinks links) {
        return new GetMandateResponse(
                mandate.getMandateId(),
                mandate.getProviderId(),
                mandate.getServiceReference(),
                mandate.getReturnUrl(),
                mandate.getMandateReference(),
                MandateStatus.valueOf(mandate.getState().getStatus().toUpperCase()),
                links);
    }

    @ApiModelProperty(value = "mandate id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "agreement_id")
    public String getAgreementId() {
        return this.agreementId;
    }

    @ApiModelProperty(value = "provider id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "provider_id")
    public String getProviderId() { return providerId; }

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "mandate state", required = true, example = "CREATED")
    @JsonProperty(value = "state")
    public MandateStatus getState() {
        return state;
    }

    @ApiModelProperty(value = "links", required = true)
    @JsonProperty(value = "_links")
    public MandateLinks getLinks() {
        return links;
    }

    @ApiModelProperty(value = "service reference", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "reference")
    public String getReference() { return reference; }

    @ApiModelProperty(value = "bank reference")
    @JsonProperty(value = "bank_statement_reference")
    public String getMandateReference() {
        return mandateReference;
    }
}
