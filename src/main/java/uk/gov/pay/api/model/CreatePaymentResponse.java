package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class CreatePaymentResponse extends LinksResponse {
    private final long paymentId;
    private final long amount;
    private final String status;

    public static CreatePaymentResponse createPaymentResponse(JsonNode payload) {
        return new CreatePaymentResponse(
                payload.get("charge_id").asLong(),
                payload.get("amount").asLong(),
                payload.get("status").asText()
        );
    }

    private CreatePaymentResponse(long chargeId, long amount, String status) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.status = status;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CreatePaymentResponse { " + super.toString() +
                ", paymentId='" + paymentId + "' " +
                ", amount='" + amount + "' " +
                ", status='" + status + "' " +
                "} ";
    }
}