package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.TransactionResponse;
import uk.gov.pay.api.model.links.PaymentLinksForSearch;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import java.net.URI;
import java.util.List;

@ApiModel(value = "PaymentDetailForSearch")
@Schema(name = "PaymentDetailForSearch")
public class PaymentForSearchResult extends CardPayment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinksForSearch links = new PaymentLinksForSearch();

    public PaymentForSearchResult(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                  String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                                  boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                                  List<PaymentConnectorResponseLink> links, URI selfLink, URI paymentEventsLink, URI paymentCancelLink, URI paymentRefundsLink, URI paymentCaptureUri,
                                  Long corporateCardSurcharge, Long totalAmount, String providerId, ExternalMetadata externalMetadata,
                                  Long fee, Long netAmount) {
        
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider,
                createdDate, refundSummary, settlementSummary, cardDetails, language, delayedCapture, corporateCardSurcharge, totalAmount, providerId, externalMetadata,
                fee, netAmount);
        this.links.addSelf(selfLink.toString());
        this.links.addEvents(paymentEventsLink.toString());
        this.links.addRefunds(paymentRefundsLink.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelLink.toString());
        }
        if (links.stream().anyMatch(link -> "capture".equals(link.getRel()))) {
            this.links.addCapture(paymentCaptureUri.toString());
        }
    }

    public static PaymentForSearchResult valueOf(
            TransactionResponse paymentResult,
            URI selfLink,
            URI paymentEventsLink,
            URI paymentCancelLink,
            URI paymentRefundsLink,
            URI paymentCaptureUri) {

        return new PaymentForSearchResult(
                paymentResult.getTransactionId(),
                paymentResult.getAmount(),
                paymentResult.getState(),
                paymentResult.getReturnUrl(),
                paymentResult.getDescription(),
                paymentResult.getReference(),
                paymentResult.getEmail(),
                paymentResult.getPaymentProvider(),
                paymentResult.getCreatedDate(),
                paymentResult.getLanguage(),
                paymentResult.getDelayedCapture(),
                paymentResult.getRefundSummary(),
                paymentResult.getSettlementSummary(),
                paymentResult.getCardDetails(),
                paymentResult.getLinks(),
                selfLink,
                paymentEventsLink,
                paymentCancelLink,
                paymentRefundsLink,
                paymentCaptureUri,
                paymentResult.getCorporateCardSurcharge(),
                paymentResult.getTotalAmount(),
                paymentResult.getGatewayTransactionId(),
                paymentResult.getMetadata().orElse(null),
                paymentResult.getFee(),
                paymentResult.getNetAmount());
    }

    @ApiModelProperty(name = LINKS_JSON_ATTRIBUTE, dataType = "uk.gov.pay.api.model.links.PaymentLinksForSearch")
    public PaymentLinksForSearch getLinks() {
        return links;
    }
}
