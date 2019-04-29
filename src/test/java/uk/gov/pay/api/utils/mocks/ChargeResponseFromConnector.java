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

public class ChargeResponseFromConnector {
    private final Long amount, corporateCardSurcharge, totalAmount; 
    private final PaymentState state; 
    private final String chargeId, returnUrl, description, reference, email, paymentProvider, gatewayTransactionId, createdDate;
    private final SupportedLanguage language; 
    private final boolean delayedCapture; 
    private final RefundSummary refundSummary; 
    private final SettlementSummary settlementSummary; 
    private final CardDetails cardDetails;
    private final List<Map<?, ?>> links;
    private final Optional<Map<String, Object>> metadata;

    public Long getAmount() {
        return amount;
    }

    public Long getCorporateCardSurcharge() {
        return corporateCardSurcharge;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public PaymentState getState() {
        return state;
    }

    public String getChargeId() {
        return chargeId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getEmail() {
        return email;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public boolean isDelayedCapture() {
        return delayedCapture;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    public SettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public List<Map<?, ?>> getLinks() {
        return links;
    }

    public Optional<Map<String, Object>> getMetadata() {
        return metadata;
    }

    private ChargeResponseFromConnector(ChargeResponseFromConnectorBuilder builder) {
        this.amount = builder.amount;
        this.chargeId = builder.chargeId;
        this.state = builder.state;
        this.returnUrl = builder.returnUrl;
        this.description = builder.description;
        this.reference = builder.reference;
        this.email = builder.email;
        this.paymentProvider = builder.paymentProvider;
        this.gatewayTransactionId = builder.gatewayTransactionId;
        this.createdDate = builder.createdDate;
        this.language = builder.language;
        this.delayedCapture = builder.delayedCapture;
        this.corporateCardSurcharge = builder.corporateCardSurcharge;
        this.totalAmount = builder.totalAmount;
        this.refundSummary = builder.refundSummary;
        this.settlementSummary = builder.settlementSummary;
        this.cardDetails = builder.cardDetails;
        this.links = builder.links;
        this.metadata = builder.metadata == null || builder.metadata.isEmpty() ? Optional.empty() : Optional.of(builder.metadata);;
    }

    public static final class ChargeResponseFromConnectorBuilder {
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

        private ChargeResponseFromConnectorBuilder() {
        }

        public static ChargeResponseFromConnectorBuilder aCreateOrGetChargeResponseFromConnector() {
            return new ChargeResponseFromConnectorBuilder();
        }

        public static ChargeResponseFromConnectorBuilder aCreateOrGetChargeResponseFromConnector(ChargeResponseFromConnector responseFromConnector) {
            return new ChargeResponseFromConnectorBuilder()
                    .withAmount(responseFromConnector.amount)
                    .withChargeId(responseFromConnector.chargeId)
                    .withState(responseFromConnector.state)
                    .withReturnUrl(responseFromConnector.returnUrl)
                    .withDescription(responseFromConnector.description)
                    .withReference(responseFromConnector.reference)
                    .withEmail(responseFromConnector.email)
                    .withPaymentProvider(responseFromConnector.paymentProvider)
                    .withGatewayTransactionId(responseFromConnector.gatewayTransactionId)
                    .withCreatedDate(responseFromConnector.createdDate)
                    .withLanguage(responseFromConnector.language)
                    .withDelayedCapture(responseFromConnector.delayedCapture)
                    .withCorporateCardSurcharge(responseFromConnector.corporateCardSurcharge)
                    .withTotalAmount(responseFromConnector.totalAmount)
                    .withRefundSummary(responseFromConnector.refundSummary)
                    .withSettlementSummary(responseFromConnector.settlementSummary)
                    .withCardDetails(responseFromConnector.cardDetails)
                    .withMetadata(responseFromConnector.metadata.orElse(null));
        }

        public ChargeResponseFromConnectorBuilder withAmount(long amount) {
            this.amount = amount;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withChargeId(String chargeId) {
            this.chargeId = chargeId;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withState(PaymentState state) {
            this.state = state;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withGatewayTransactionId(String gatewayTransactionId) {
            this.gatewayTransactionId = gatewayTransactionId;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withLanguage(SupportedLanguage language) {
            this.language = language;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withDelayedCapture(boolean delayedCapture) {
            this.delayedCapture = delayedCapture;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withCorporateCardSurcharge(Long corporateCardSurcharge) {
            this.corporateCardSurcharge = corporateCardSurcharge;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withRefundSummary(RefundSummary refundSummary) {
            this.refundSummary = refundSummary;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withSettlementSummary(SettlementSummary settlementSummary) {
            this.settlementSummary = settlementSummary;
            return this;
        }

        public ChargeResponseFromConnectorBuilder withCardDetails(CardDetails cardDetails) {
            this.cardDetails = cardDetails;
            return this;
        }

        public ChargeResponseFromConnector build() {
            List.of(amount, chargeId, language, cardDetails, links).forEach(Objects::requireNonNull);
            
            return new ChargeResponseFromConnector(this);
        }

        public ChargeResponseFromConnectorBuilder withLink(Map<String, ?> link) {
            links.add(link);
            return this;
        }

        public ChargeResponseFromConnectorBuilder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
    }
}
