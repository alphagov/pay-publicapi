package uk.gov.pay.api.service;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class PublicApiUriGenerator {
    public URI getPaymentURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}")
                .build(chargeId);
    }

    public URI getPaymentEventsURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/events")
                .build(chargeId);
    }

    public URI getPaymentCancelURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/cancel")
                .build(chargeId);
    }

    public URI getPaymentRefundsURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/refunds")
                .build(chargeId);
    }

}
