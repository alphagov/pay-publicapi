package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

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

    public String convertHostToPublicAPI(String link) {
        URI originalUri = UriBuilder.fromUri(link).build();
        URI newUri = UriBuilder.fromUri(baseUrl)
                .path(originalUri.getPath())
                .replaceQuery(originalUri.getQuery())
                .build();
        try {
            return URLDecoder.decode(newUri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This shouldn't be reached. This is only thrown if 'character encoding needs to be consulted, but
            // named character encoding is not supported' within the .decode() method above. This is using UTF-8 which does exist.
            throw new RuntimeException(e.getMessage());
        }
    }

}
