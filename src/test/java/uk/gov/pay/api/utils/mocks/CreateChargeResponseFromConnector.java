package uk.gov.pay.api.utils.mocks;

import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CreateChargeResponseFromConnector {
    public final Long amount, corporateCardSurcharge, totalAmount; 
    public final PaymentState state; 
    public final String chargeId, returnUrl, description, reference, email, paymentProvider, gatewayTransactionId, createdDate;
    public final SupportedLanguage language; 
    public final boolean delayedCapture; 
    public final RefundSummary refundSummary; 
    public final SettlementSummary settlementSummary; 
    public final CardDetails cardDetails;
    public final List<Map<?, ?>> links;
    public final Optional<Map<String, Object>> metadata;

    private CreateChargeResponseFromConnector(long amount, String chargeId, PaymentState state, String returnUrl,
                                              String description, String reference, String email, String paymentProvider,
                                              String gatewayTransactionId, String createdDate, SupportedLanguage language,
                                              boolean delayedCapture, Long corporateCardSurcharge, Long totalAmount,
                                              RefundSummary refundSummary, SettlementSummary settlementSummary,
                                              CardDetails cardDetails, List<Map<?, ?>> links, Map<String, Object> metadata) {
        this.amount = amount;
        this.chargeId = chargeId;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.email = email;
        this.paymentProvider = paymentProvider;
        this.gatewayTransactionId = gatewayTransactionId;
        this.createdDate = createdDate;
        this.language = language;
        this.delayedCapture = delayedCapture;
        this.corporateCardSurcharge = corporateCardSurcharge;
        this.totalAmount = totalAmount;
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.cardDetails = cardDetails;
        this.links = links;
        this.metadata = metadata == null || metadata.isEmpty() ? Optional.empty() : Optional.of(metadata);;
    }

    public static final class CreateChargeResponseFromConnectorBuilder {
        private Long amount, corporateCardSurcharge, totalAmount;
        private PaymentState state;
        private String chargeId, returnUrl, description, reference, email, paymentProvider, gatewayTransactionId, createdDate;
        private SupportedLanguage language;
        private Boolean delayedCapture;
        private RefundSummary refundSummary;
        private SettlementSummary settlementSummary;
        private CardDetails cardDetails;
        private List<Map<?, ?>> links = new ArrayList<>();
        private Map<String, Object> metadata = Map.of();

        private CreateChargeResponseFromConnectorBuilder() {
        }

        public static CreateChargeResponseFromConnectorBuilder aCreateChargeResponseFromConnector() {
            return new CreateChargeResponseFromConnectorBuilder();
        }

        public CreateChargeResponseFromConnectorBuilder withAmount(long amount) {
            this.amount = amount;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withChargeId(String chargeId) {
            this.chargeId = chargeId;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withState(PaymentState state) {
            this.state = state;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withGatewayTransactionId(String gatewayTransactionId) {
            this.gatewayTransactionId = gatewayTransactionId;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withLanguage(SupportedLanguage language) {
            this.language = language;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withDelayedCapture(boolean delayedCapture) {
            this.delayedCapture = delayedCapture;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withCorporateCardSurcharge(Long corporateCardSurcharge) {
            this.corporateCardSurcharge = corporateCardSurcharge;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withRefundSummary(RefundSummary refundSummary) {
            this.refundSummary = refundSummary;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withSettlementSummary(SettlementSummary settlementSummary) {
            this.settlementSummary = settlementSummary;
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withCardDetails(CardDetails cardDetails) {
            this.cardDetails = cardDetails;
            return this;
        }

        public CreateChargeResponseFromConnector build() {
            List.of(amount, chargeId, language, cardDetails, links).forEach(Objects::requireNonNull);
            
            return new CreateChargeResponseFromConnector(amount, chargeId, state, returnUrl, description, reference, email, 
                    paymentProvider, gatewayTransactionId, createdDate, language, delayedCapture, corporateCardSurcharge, 
                    totalAmount, refundSummary, settlementSummary, cardDetails, links, metadata);
        }

        public CreateChargeResponseFromConnectorBuilder withLink(Map<String, ?> link) {
            links.add(link);
            return this;
        }

        public CreateChargeResponseFromConnectorBuilder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
    }
}
