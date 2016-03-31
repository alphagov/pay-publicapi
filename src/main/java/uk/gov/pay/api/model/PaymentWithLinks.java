package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Optional;

public class PaymentWithLinks implements PaymentWithLinksJSON {
    Payment payment;

    private final Links links = new Links();

    public Links getLinks() {
        return links;
    }

    public static PaymentWithLinks createPaymentResponseWithLinks(JsonNode payload, String selfLink) {
        Payment payment = Payment.createPaymentResponse(payload);
        PaymentWithLinks paymentWithLinks = new PaymentWithLinks(payment);
        paymentWithLinks.withSelfLink(selfLink);
        Optional<JsonNode> nextLinkMaybe = getNextLink(payload);

        nextLinkMaybe.ifPresent(
                (nextLink) -> paymentWithLinks.withNextLink(nextLink.get("href").asText()));

        return paymentWithLinks;
    }

    private PaymentWithLinks(Payment payment) {
        this.payment = payment;
    }

    public String getCreatedDate() {
        return payment.getCreatedDate();
    }

    public String getPaymentId() {
        return payment.getPaymentId();
    }

    public long getAmount() {
        return payment.getAmount();
    }

    public String getStatus() {
        return payment.getStatus();
    }

    public String getReturnUrl() {
        return payment.getReturnUrl();
    }

    public String getDescription() {
        return payment.getDescription();
    }

    public String getReference() {
        return payment.getReference();
    }

    public String getPaymentProvider() {
        return payment.getPaymentProvider();
    }

    public PaymentWithLinks withSelfLink(String url) {
        this.links.setSelf(url);
        return this;
    }

    public PaymentWithLinks withNextLink(String url) {
        this.links.setNextUrl(url);
        return this;
    }

    private static Optional<JsonNode> getNextLink(JsonNode payload) {
        for (Iterator<JsonNode> it = payload.get("links").elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            if ("next_url".equals(node.get("rel").asText())) {
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }

}
