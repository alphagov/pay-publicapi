package uk.gov.pay.api.auth;

import uk.gov.pay.api.model.TokenPaymentType;

import java.security.Principal;

public class Account implements Principal {

    private final String name;
    private final TokenPaymentType paymentType;

    public Account(String name, TokenPaymentType paymentType) {
        this.name = name;
        this.paymentType = paymentType;
    }

    @Override
    public String getName() {
        return name;
    }

    public TokenPaymentType getPaymentType() {
        return paymentType;
    }
}
