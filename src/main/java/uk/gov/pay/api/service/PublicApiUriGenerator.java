package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
    public URI getRefundsURI(String chargeId, String refundId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/refunds/{refunds}")
                .build(chargeId, refundId);
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

    public URI getSearchMandatesURIWithQueryOf(String query) {
        return UriBuilder.fromUri(baseUrl)
                .replaceQuery(query)
                .path("/v1/directdebit/mandates")
                .build();
    }

    public URI getMandateURI(String mandateId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/mandates/{mandateId}")
                .build(mandateId);
    }
    
    public URI getPaymentCaptureURI(String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/payments/{paymentId}/capture")
                .build(chargeId);
    }
    
    public URI getDirectDebitPaymentURI(String paymentId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/payments/{paymentId}")
                .build(paymentId);
    }

    public URI getDirectDebitPaymentEventsURI(String paymentId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/payments/{paymentId}/events")
                .build(paymentId);
    }

    public String convertHostToPublicAPI(String link) {
        URI originalUri = UriBuilder.fromUri(link).build();
        URI newUri = UriBuilder.fromUri(baseUrl)
                .path(originalUri.getPath())
                .replaceQuery(originalUri.getQuery())
                .build();
        return URLDecoder.decode(newUri.toString(), StandardCharsets.UTF_8);
    }

    public URI getMandatePaymentsURI(String mandateId) {
        return UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/payments")
                .queryParam("mandate_id", mandateId)
                .build(mandateId);
    }
}
