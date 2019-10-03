package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import static uk.gov.pay.api.model.links.directdebit.MandateLinks.MandateLinksBuilder.aMandateLinks;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private final Payer payer;

    public MandateResponse(MandateConnectorResponse mandate, PublicApiUriGenerator publicApiUriGenerator) {
        var mandateLinks = aMandateLinks()
                .withSelf(publicApiUriGenerator.getMandateURI(mandate.getMandateId()).toString())
                .withPayments(publicApiUriGenerator.getMandatePaymentsURI(mandate.getMandateId()).toString())
                .withNextUrl(mandate.getLinks())
                .withNextUrlPost(mandate.getLinks())
                .build();

        this.mandateId = mandate.getMandateId();
        this.providerId = mandate.getProviderId();
        this.reference = mandate.getServiceReference();
        this.returnUrl = mandate.getReturnUrl();
        this.state = MandateStatus.valueOf(mandate.getState());
        this.mandateReference = mandate.getMandateReference();
        this.createdDate = mandate.getCreatedDate();
        this.description = mandate.getDescription();
        this.paymentProvider = mandate.getPaymentProvider();
        this.links = mandateLinks;
        this.payer = mandate.getPayer();
    }

    @ApiModelProperty(value = "Your user's name and email address.")
    @JsonProperty(value = "payer")
    public Payer getPayer() {
        return payer;
    }

    @ApiModelProperty(value = "The human-readable description of the mandate.")
    @JsonProperty(value = "description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "This is `GoCardless`.")
    @JsonProperty(value = "payment_provider")
    public String getPaymentProvider() {
        return paymentProvider;
    }

    @ApiModelProperty(value = "The date you requested the mandate.")
    @JsonProperty("created_date")
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(value = "The reference number of the mandate.", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "mandate_id")
    public String getMandateId() {
        return this.mandateId;
    }

    @ApiModelProperty(value = "The reference number GoCardless associated with the mandate.", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "provider_id")
    public String getProviderId() { return providerId; }

    @ApiModelProperty(value = "An HTTPS URL on your site that your user will be sent back to once they have confirmed the mandate on GOV.UK Pay.", example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "The `status` of the mandate, and the reason for the status.", example = "CREATED")
    @JsonProperty(value = "state")
    public MandateStatus getState() {
        return state;
    }

    @ApiModelProperty(value = "API endpoints related to this mandate.")
    @JsonProperty(value = "_links")
    public MandateLinks getLinks() {
        return links;
    }

    @ApiModelProperty(value = "The reference number you associated with this mandate.", example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = "reference")
    public String getReference() { return reference; }

    @ApiModelProperty(value = "The description that may appear on your user's bank statement. Your user may not see the full description.")
    @JsonProperty(value = "bank_statement_reference")
    public String getMandateReference() {
        return mandateReference;
    }

}
