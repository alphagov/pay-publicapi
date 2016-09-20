package uk.gov.pay.api.model;

import black.door.hate.HalResource;
import uk.gov.pay.api.resources.RefundsFromConnector;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static black.door.hate.HalRepresentation.HalRepresentationBuilder;
import static black.door.hate.HalRepresentation.builder;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_BY_ID_PATH;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_REFUNDS_PATH;

public class RefundsResponse extends HalResourceResponse {

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
