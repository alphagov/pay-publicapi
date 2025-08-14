package uk.gov.pay.api.auth;

import uk.gov.pay.api.model.TokenPaymentType;

import java.security.Principal;

public record Account(String accountId, TokenPaymentType paymentType, String tokenLink) implements Principal {

    @Override
    public String getName() {
        return accountId();
    }

}
