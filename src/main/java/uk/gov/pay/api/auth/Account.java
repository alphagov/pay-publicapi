package uk.gov.pay.api.auth;

import uk.gov.pay.commons.model.TokenPaymentType;

import java.security.Principal;

public class Account implements Principal {

    private final String accountId;
    private final TokenPaymentType paymentType;

    public Account(String accountId, TokenPaymentType paymentType) {
        this.accountId = accountId;
        this.paymentType = paymentType;
    }

    @Override
    public String getName() {
        return getAccountId();
    }
    
    public String getAccountId() {
        return accountId;
    }

    public TokenPaymentType getPaymentType() {
        return paymentType;
    }
}
