package uk.gov.pay.api.model;

import java.net.URI;
import java.util.List;

public class PaymentWithLinks implements PaymentWithLinksJSON {

    private Payment payment;
    private Links links = new Links();

    public static PaymentWithLinks valueOf(PaymentConnectorResponse paymentConnector, URI selfLink) {
        return new PaymentWithLinks(Payment.valueOf(paymentConnector), paymentConnector.getLinks(), selfLink);
    }

    private PaymentWithLinks(Payment payment, List<PaymentConnectorResponseLink> paymentConnectorResponseLinks, URI selfLink) {
        this.payment = payment;
        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
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

    public Links getLinks() {
        return links;
    }
}
