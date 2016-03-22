package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

public class PaymentWithLinks implements PaymentWithLinksJSON {
    Payment payment;

    private final Links links = new Links();

    public Links getLinks() {
        return links;
    }

    public static PaymentWithLinks valueOf(JsonNode payload, URI selfLink) {
        PaymentWithLinks payment = new PaymentWithLinks(Payment.valueOf(payload, selfLink));
        payment.withSelfLink(selfLink.toString());
        JsonNode links = payload.get("links");
        links.forEach(link -> {
            String rel = link.get("rel").asText();
            if ("next_url".equals(rel)) {
                payment.withNextLink(link.get("href").asText());
            }
            if ("next_url_post".equals(rel)) {
                payment.withNextPostLink(link.get("href").asText(), link.get("type").asText(), link.get("params"));
            }
        });
        return payment;
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

    public PaymentWithLinks withNextPostLink(String url, String type, JsonNode params) {
        this.links.setNextUrlPost(url, type, params);
        return this;
    }
}
