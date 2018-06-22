package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class PublicApiUriGenerator {

    private final String baseUrl;

    @Inject
    public PublicApiUriGenerator(PublicApiConfig configuration) {
        this.baseUrl = configuration.getBaseUrl();
    }
    
    public URI getPaymentURI(String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}")
                .build(chargeId);
    }

    public URI getPaymentEventsURI(String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/events")
                .build(chargeId);
    }

    public URI getPaymentCancelURI(String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/cancel")
                .build(chargeId);
    }

    public URI getPaymentRefundsURI(String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/refunds")
                .build(chargeId);
    }
    
    public URI getAgreementURI(String agreementId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/agreements/{agreementId}")
                .build(agreementId);
    }

}
