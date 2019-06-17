package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.PaymentProvider;
import uk.gov.pay.api.model.links.directdebit.AgreementLinks;

@ApiModel(value = "CreateAgreementResponse", description = "The Agreement Payload to create a new Agreement")
public class CreateAgreementResponse {

    private static final String MANDATE_ID_FIELD_NAME = "mandate_id";
    private static final String PROVIDER_ID_FIELD_NAME = "provider_id";
    private static final String REFERENCE_FIELD_NAME = "reference";
    private static final String RETURN_URL_FIELD_NAME = "return_url";
    private static final String CREATED_DATE_FIELD_NAME = "created_date";
    private static final String STATE_FIELD_NAME = "state";
    private static final String LINKS_FIELD_NAME = "_links";
    private static final String DESCRIPTION_NAME = "description";
    private static final String PAYMENT_PROVIDER_NAME= "payment_provider";

    private final String agreementId;
    private final String providerId;
    private final String reference;
    private final String returnUrl;
    private final String createdDate;
    private final AgreementStatus state;
    private final AgreementLinks links;
    private final String description;
    private final String paymentProvider = PaymentProvider.GOCARDLESS.getName();

    private CreateAgreementResponse(String agreementId,
                                    String providerId,
                                    String reference,
                                    String returnUrl,
                                    String createdDate,
                                    AgreementStatus state,
                                    AgreementLinks links, 
                                    String description) {
        this.agreementId = agreementId;
        this.providerId = providerId;
        this.reference = reference;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
        this.links = links;
        this.description = description;
    }

    public static CreateAgreementResponse from(MandateConnectorResponse mandate, AgreementLinks links) {
        return new CreateAgreementResponse(
                mandate.getMandateId(),
                mandate.getMandateReference(),
                mandate.getServiceReference(),
                mandate.getReturnUrl(),
                mandate.getCreatedDate(),
                AgreementStatus.valueOf(mandate.getState().getStatus().toUpperCase()),
                links, 
                mandate.getDescription());
    }

    @ApiModelProperty(value = "mandate id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = MANDATE_ID_FIELD_NAME)
    public String getMandateId() {
        return agreementId;
    }

    @ApiModelProperty(value = "provider id", required = true, example = "jhjcvaiqlediuhh23d89hd3")
    @JsonProperty(value = PROVIDER_ID_FIELD_NAME)
    public String getProviderId() {
        return providerId;
    }

    @ApiModelProperty(value = "agreement reference", example = "test_service_reference")
    @JsonProperty(value = REFERENCE_FIELD_NAME)
    public String getReference() {
        return reference;
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

    @ApiModelProperty(value = "description")
    @JsonProperty(value = DESCRIPTION_NAME)
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "payment_provider")
    @JsonProperty(value = PAYMENT_PROVIDER_NAME)
    public String getPaymentProvider() {
        return paymentProvider;
    }
}
