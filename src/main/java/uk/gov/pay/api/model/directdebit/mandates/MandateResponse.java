package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static uk.gov.pay.api.model.links.directdebit.MandateLinks.MandateLinksBuilder.aMandateLinks;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema
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

    @ApiModelProperty(value = "payer")
    @Schema(description = "payer")
    @JsonProperty(value = "payer")
    public Payer getPayer() {
        return payer;
    }

    @ApiModelProperty(value = "description")
    @Schema(description = "description", accessMode = READ_ONLY)
    @JsonProperty(value = "description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "payment_provider")
    @Schema(description = "payment_provider", accessMode = READ_ONLY)
    @JsonProperty(value = "payment_provider")
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    @ApiModelProperty(value = "mandate created date")
    @Schema(description = "mandate created date", accessMode = READ_ONLY)
    @JsonProperty("created_date")
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(value = "mandate id", example = "jhjcvaiqlediuhh23d89hd3")
    @Schema(description = "mandate id", example = "jhjcvaiqlediuhh23d89hd3", accessMode = READ_ONLY)
    @JsonProperty(value = "mandate_id")
    public String getMandateId() {
        return this.mandateId;
    }

    @ApiModelProperty(value = "provider id", example = "jhjcvaiqlediuhh23d89hd3")
    @Schema(description = "provider id", example = "jhjcvaiqlediuhh23d89hd3", accessMode = READ_ONLY)
    @JsonProperty(value = "provider_id")
    public String getProviderId() { return providerId; }

    @ApiModelProperty(value = "service return url", example = "https://service-name.gov.uk/transactions/12345")
    @Schema(description = "service return url", example = "https://service-name.gov.uk/transactions/12345", accessMode = READ_ONLY)
    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "mandate state", example = "CREATED")
    @Schema(description = "mandate state")
    @JsonProperty(value = "state")
    public MandateStatus getState() {
        return state;
    }

    @ApiModelProperty(value = "links")
    @Schema(name = "_links", description = "payment, events, self and next links of a Mandate")
    @JsonProperty(value = "_links")
    public MandateLinks getLinks() {
        return links;
    }

    @ApiModelProperty(value = "service reference", example = "jhjcvaiqlediuhh23d89hd3")
    @Schema(description = "service reference", example = "jhjcvaiqlediuhh23d89hd3", accessMode = READ_ONLY)
    @JsonProperty(value = "reference")
    public String getReference() { return reference; }

    @ApiModelProperty(value = "This value comes from GoCardless when a mandate has been created.")
    @Schema(description = "This value comes from GoCardless when a mandate has been created.", accessMode = READ_ONLY)
    @JsonProperty(value = "bank_statement_reference")
    public String getMandateReference() {
        return mandateReference;
    }

}
