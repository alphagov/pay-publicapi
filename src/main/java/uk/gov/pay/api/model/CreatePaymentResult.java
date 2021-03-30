package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.PaymentLinks;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static uk.gov.pay.api.model.Payment.LINKS_JSON_ATTRIBUTE;

/**
 * Defines swagger specs for create payment
 */
public class CreatePaymentResult {

    @JsonProperty
    @Schema(name = "amount", description = "The amount in pence.", example = "1200")
    private long amount;

    @JsonProperty
    private PaymentState state;

    @JsonProperty
    @Schema(description = "The human-readable description you gave the payment.", example = "New passport application")
    private String description;

    @JsonProperty
    @Schema(description = "The reference number you associated with this payment.", example = "12345")
    private String reference;

    @JsonProperty
    @Schema(name = "language", description = "Which language your users will see on the payment pages when they make a payment.", example = "en")
    private SupportedLanguage language;

    @JsonProperty
    @Schema(name = "payment_id", description = "The unique identifier of the payment.", example = "hu20sqlact5260q2nanm0q8u93")
    private String paymentId;

    @JsonProperty
    @Schema(name = "payment_provider", example = "worldpay")
    private String paymentProvider;

    @JsonProperty
    @Schema(name = "return_url", description = "An HTTPS URL on your site that your user will be sent back to once they have completed their payment attempt on GOV.UK Pay.", example = "https://service-name.gov.uk/transactions/12345")
    private String returnUrl;

    @JsonProperty
    @Schema(name = "created_date", description = "The date you created the payment.", example = "2016-01-21T17:15:00Z")
    private String createdDate;

    @JsonProperty
    @Schema(name = "delayed_capture", description = "Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.", example = "false", accessMode = READ_ONLY)
    private boolean delayedCapture;

    @JsonProperty
    @Schema(description = "Mail Order / Telephone Order (MOTO) payment flag.", example = "false")
    private boolean moto;

    @JsonProperty("refund_summary")
    private RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private PaymentSettlementSummary settlementSummary;

    @JsonProperty
    @Schema(name = LINKS_JSON_ATTRIBUTE, description = "API endpoints related to the payment.")
    private PaymentLinks links;

    @JsonProperty
    @Schema(name = "provider_id", description = "The reference number the payment gateway associated with the payment.", example = "null")
    private String providerId;

    @JsonProperty
    @Schema(name = "metadata", description = "[Custom metadata](https://docs.payments.service.gov.uk/optional_features/custom_metadata/) you added to the payment.")
    private ExternalMetadata metadata;

    @JsonProperty
    @Schema(name = "email", description = "The email address of your user.", example = "citizen@example.org")
    private String email;

    @JsonProperty(value = "card_details")
    @Schema(name = "card_details")
    private CardDetails cardDetails;
}
