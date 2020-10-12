package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.PaymentLinks;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static uk.gov.pay.api.model.Payment.LINKS_JSON_ATTRIBUTE;

/**
 * Defines swagger specs for create payment
 */
@ApiModel
public class CreatePaymentResult {

    @JsonProperty
    @ApiModelProperty(name = "amount", value = "The amount in pence.", example = "1200")
    @Schema(name = "amount", description = "The amount in pence.", example = "1200")
    private long amount;

    @JsonProperty
    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentState")
    private PaymentState state;

    @JsonProperty
    @ApiModelProperty(value = "The human-readable description you gave the payment.", example = "New passport application")
    @Schema(description = "The human-readable description you gave the payment.", example = "New passport application")
    private String description;

    @JsonProperty
    @ApiModelProperty(value = "The reference number you associated with this payment.", example = "12345")
    @Schema(description = "The reference number you associated with this payment.", example = "12345")
    private String reference;

    @JsonProperty
    @ApiModelProperty(name = "language", value = "Which language your users will see on the payment pages when they make a payment.", access = "language", example = "en", allowableValues = "en,cy")
    @Schema(name = "language", description = "Which language your users will see on the payment pages when they make a payment.", example = "en")
    private SupportedLanguage language;

    @JsonProperty
    @ApiModelProperty(name = "payment_id", value = "The unique identifier of the payment.", example = "hu20sqlact5260q2nanm0q8u93")
    @Schema(name = "payment_id", description = "The unique identifier of the payment.", example = "hu20sqlact5260q2nanm0q8u93")
    private String paymentId;

    @JsonProperty
    @ApiModelProperty(name = "payment_provider", example = "worldpay")
    @Schema(name = "payment_provider", example = "worldpay")
    private String paymentProvider;

    @JsonProperty
    @ApiModelProperty(name = "return_url", value = "An HTTPS URL on your site that your user will be sent back to once they have completed their payment attempt on GOV.UK Pay.", example = "https://service-name.gov.uk/transactions/12345")
    @Schema(name = "return_url", description = "An HTTPS URL on your site that your user will be sent back to once they have completed their payment attempt on GOV.UK Pay.", example = "https://service-name.gov.uk/transactions/12345")
    private String returnUrl;

    @JsonProperty
    @ApiModelProperty(name = "created_date", value = "The date you created the payment.", example = "2016-01-21T17:15:00Z")
    @Schema(name = "created_date", description = "The date you created the payment.", example = "2016-01-21T17:15:00Z")
    private String createdDate;

    @JsonProperty
    @ApiModelProperty(name = "delayed_capture", access = "delayed_capture", value = "Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.", example = "false")
    @Schema(name = "delayed_capture", description = "Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.", example = "false", accessMode = READ_ONLY)
    private boolean delayedCapture;

    @JsonProperty
    @ApiModelProperty(value = "Mail Order / Telephone Order (MOTO) payment flag.", example = "false")
    @Schema(description = "Mail Order / Telephone Order (MOTO) payment flag.", example = "false")
    private boolean moto;

    @JsonProperty("refund_summary")
    private RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private PaymentSettlementSummary settlementSummary;

    @JsonProperty
    @ApiModelProperty(name = LINKS_JSON_ATTRIBUTE, value = "API endpoints related to the payment.")
    @Schema(name = LINKS_JSON_ATTRIBUTE, description = "API endpoints related to the payment.")
    private PaymentLinks links;

    @JsonProperty
    @ApiModelProperty(name = "provider_id", value = "The reference number the payment gateway associated with the payment.", example = "null")
    @Schema(name = "provider_id", description = "The reference number the payment gateway associated with the payment.", example = "null")
    private String providerId;

    @JsonProperty
    @ApiModelProperty(name = "metadata", value = "[Custom metadata](https://docs.payments.service.gov.uk/optional_features/custom_metadata/) you added to the payment.", dataType = "Map[String,String]")
    @Schema(name = "metadata", description = "[Custom metadata](https://docs.payments.service.gov.uk/optional_features/custom_metadata/) you added to the payment.")
    private ExternalMetadata metadata;

    @JsonProperty
    @ApiModelProperty(name = "email", value = "The email address of your user.", example = "citizen@example.org")
    @Schema(name = "email", description = "The email address of your user.", example = "citizen@example.org")
    private String email;

    @JsonProperty(value = "card_details")
    @ApiModelProperty(name = "card_details")
    @Schema(name = "card_details")
    private CardDetails cardDetails;
}
