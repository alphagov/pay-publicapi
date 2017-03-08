package uk.gov.pay.api.auth;

import java.security.Principal;

public class Account implements Principal {

    private final String name;

    public Account(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
