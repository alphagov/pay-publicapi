package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentLinks;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import static uk.gov.pay.api.model.Payment.LINKS_JSON_ATTRIBUTE;

/**
 * Defines swagger specs for create payment
 */
@ApiModel
public class CreatePaymentResult {

    @JsonProperty
    @ApiModelProperty(name = "amount", example = "1200")
    private long amount;

    @JsonProperty
    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentState")
    private PaymentState state;

    @JsonProperty
    @ApiModelProperty(example = "Your Service Description")
    private String description;

    @JsonProperty
    @ApiModelProperty(example = "your-reference")
    private String reference;

    @JsonProperty
    @ApiModelProperty(name = "language", access = "language", example = "en", allowableValues = "en,cy")
    private SupportedLanguage language;

    @JsonProperty
    @ApiModelProperty(name = "payment_id", example = "hu20sqlact5260q2nanm0q8u93")
    private String paymentId;

    @JsonProperty
    @ApiModelProperty(name = "payment_provider", example = "worldpay")
    private String paymentProvider;

    @JsonProperty
    @ApiModelProperty(name = "return_url", example = "http://your.service.domain/your-reference")
    private String returnUrl;

    @JsonProperty
    @ApiModelProperty(name = "created_date", example = "2016-01-21T17:15:00Z")
    private String createdDate;

    @JsonProperty
    @ApiModelProperty(name = "delayed_capture", access = "delayed_capture")
    private boolean delayedCapture;

    @JsonProperty("refund_summary")
    private RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private SettlementSummary settlementSummary;

    @JsonProperty
    @ApiModelProperty(name = LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links;

    @JsonProperty
    @ApiModelProperty(name = "provider_id", example = "null")
    private String providerId;

    @JsonProperty
    @ApiModelProperty(name = "metadata", dataType = "Map[String,String]")
    private ExternalMetadata metadata;

    @JsonProperty
    @ApiModelProperty(name = "email", example = "citizen@example.org", required = false)
    private String email;

    @JsonProperty(value = "card_details")
    @ApiModelProperty(name = "card_details", required = false)
    private CardDetails cardDetails;
}
