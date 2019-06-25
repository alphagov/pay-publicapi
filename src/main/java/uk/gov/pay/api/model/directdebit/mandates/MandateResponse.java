package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;

public class MandateResponse {

    private final String mandateId;
    private final String providerId;
    private final String reference;
    private final String returnUrl;
    private final MandateStatus state;
    private final MandateLinks links;
    private final String mandateReference;
    private final String createdDate;
    private final String description;
    private final String paymentProvider;

    public MandateResponse(MandateConnectorResponse mandate, MandateLinks links) {
        this.mandateId = mandate.getMandateId();
        this.providerId = mandate.getProviderId();
        this.reference = mandate.getServiceReference();
        this.returnUrl = mandate.getReturnUrl();
        this.state = MandateStatus.valueOf(mandate.getState());
        this.mandateReference = mandate.getMandateReference();
        this.createdDate = mandate.getCreatedDate();
        this.description = mandate.getDescription();
        this.paymentProvider = mandate.getPaymentProvider();
        this.links = links;
    }

    @ApiModelProperty(value = "description")
    @JsonProperty(value = "description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "payment_provider")
    @JsonProperty(value = "payment_provider")
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    @ApiModelProperty(value = "mandate created date", required = true)
    @JsonProperty("created_date")
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(value = "mandate id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "mandate_id")
    public String getMandateId() {
        return this.mandateId;
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
