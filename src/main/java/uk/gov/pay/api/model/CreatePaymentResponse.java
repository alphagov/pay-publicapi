package uk.gov.pay.api.model;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class CreatePaymentResponse extends LinksResponse {
    private final String paymentId;

    public CreatePaymentResponse(String chargeId) {
        this.paymentId = chargeId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    @Override
    public String toString() {
        return "CreatePaymentResponse { " + super.toString() +
                ", paymentId='" + paymentId + "' " +
                "} ";
    }
}