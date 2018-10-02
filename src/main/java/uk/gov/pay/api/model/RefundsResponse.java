package uk.gov.pay.api.model;

import black.door.hate.HalResource;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static black.door.hate.HalRepresentation.HalRepresentationBuilder;
import static black.door.hate.HalRepresentation.builder;

public class RefundsResponse extends HalResourceResponse {

    private static final String API_VERSION_PATH = "/v1";
    private static final String PATH_PAYMENT_KEY = "paymentId";
    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PATH_PAYMENT_KEY + "}";
    public static final String PAYMENT_BY_ID_PATH = API_VERSION_PATH + "/payments/" + PAYMENTS_ID_PLACEHOLDER;
    public static final String PAYMENT_REFUNDS_PATH = PAYMENT_BY_ID_PATH + "/refunds";

    private RefundsResponse(HalRepresentationBuilder refundHalRepresentation, URI location) {
        super(refundHalRepresentation, location);
    }

    public static RefundsResponse valueOf(RefundsFromConnector refundsEntity, String baseUrl) {

        URI selfLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_REFUNDS_PATH)
                .build(refundsEntity.getPaymentId());

        URI paymentLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_BY_ID_PATH)
                .build(refundsEntity.getPaymentId());

        List<HalResource> refundHalResources = refundsEntity.getEmbedded().getRefunds().stream()
                .map(refund -> RefundResponse.valueOf(refund, refundsEntity.getPaymentId(), baseUrl))
                .collect(Collectors.toList());

        HalRepresentationBuilder refundHalRepresentation = builder()
                .addProperty("payment_id", refundsEntity.getPaymentId())
                .addLink("payment", paymentLink)
                .addEmbedded("refunds", refundHalResources);

        return new RefundsResponse(refundHalRepresentation, selfLink);
    }
}
