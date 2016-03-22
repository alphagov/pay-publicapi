package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

public class Payment implements PaymentJSON {
    private final String paymentId;

    private final String paymentProvider;
    private final long amount;
    private final String status;
    private final String description;

    private final String returnUrl;

    private final String reference;

    private final String createdDate;

    public static Payment valueOf(JsonNode payload, URI documentLocation) {
        return new Payment(
                payload.get("charge_id").asText(),
                payload.get("amount").asLong(),
                payload.get("status").asText(),
                payload.get("return_url").asText(),
                payload.get("description").asText(),
                payload.get("reference").asText(),
                payload.get("payment_provider").asText(),
                payload.get("created_date").asText()
        );
    }

    private Payment(String chargeId, long amount, String status, String returnUrl, String description,
                    String reference, String paymentProvider, String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.status = status;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
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

    public String getPaymentProvider() {
        return paymentProvider;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
