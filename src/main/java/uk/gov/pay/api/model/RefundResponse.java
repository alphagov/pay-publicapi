package uk.gov.pay.api.model;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static black.door.hate.HalRepresentation.HalRepresentationBuilder;
import static black.door.hate.HalRepresentation.builder;
import static uk.gov.pay.api.model.RefundsResponse.PAYMENT_BY_ID_PATH;
import static uk.gov.pay.api.model.RefundsResponse.PAYMENT_REFUNDS_PATH;

public class RefundResponse extends HalResourceResponse {

    public static final String PAYMENT_REFUND_BY_ID_PATH = PAYMENT_REFUNDS_PATH + "/{refundId}";
    
    private RefundResponse(HalRepresentationBuilder refundHalRepresentation, URI location) {
        super(refundHalRepresentation, location);
    }

    public static RefundResponse valueOf(RefundFromConnector refundEntity, String paymentId, String baseUrl) {
        URI selfLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_REFUND_BY_ID_PATH)
                .build(paymentId, refundEntity.getRefundId());

        URI paymentLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_BY_ID_PATH)
                .build(paymentId);

        return new RefundResponse(builder()
                .addProperty("refund_id", refundEntity.getRefundId())
                .addProperty("amount", refundEntity.getAmount())
                .addProperty("status", refundEntity.getStatus())
                .addProperty("created_date", refundEntity.getCreatedDate())
                .addLink("payment", paymentLink), selfLink);
    }
}
