package uk.gov.pay.api.model;

import java.net.URI;

public class PaymentWithSelfLinks implements PaymentWithSelfLinksJSON, PaymentJSON {

    private Payment payment;
    private SelfLinks links = new SelfLinks();

    public static PaymentWithSelfLinks valueOf(PaymentConnectorResponse charge, URI selfLink) {
        return new PaymentWithSelfLinks(Payment.valueOf(charge), selfLink);
    }

    private PaymentWithSelfLinks(Payment payment, URI selfLink) {
        this.payment = payment;
        this.links.addSelf(selfLink.toString());
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

    public SelfLinks getLinks() {
        return links;
    }
}
