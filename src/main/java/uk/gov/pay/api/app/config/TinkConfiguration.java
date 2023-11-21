package uk.gov.pay.api.app.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TinkConfiguration {

    @Valid
    @NotNull
    private String clientId;

    @Valid
    @NotNull
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
