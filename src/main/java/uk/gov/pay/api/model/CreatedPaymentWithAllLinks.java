package uk.gov.pay.api.model;

public class CreatedPaymentWithAllLinks {

    public enum WhenCreated {
        BRAND_NEW,
        EXISTING
    }

    private final CardPayment payment;
    private final WhenCreated whenCreated;

    private CreatedPaymentWithAllLinks(CardPayment payment, WhenCreated whenCreated) {
        this.payment = payment;
        this.whenCreated = whenCreated;
    }

    public static CreatedPaymentWithAllLinks of(CardPayment payment, WhenCreated whenCreated) {
        return new CreatedPaymentWithAllLinks(payment, whenCreated);
    }

    public CardPayment getPayment() {
        return payment;
    }

    public WhenCreated getWhenCreated() {
        return whenCreated;
    }
}
