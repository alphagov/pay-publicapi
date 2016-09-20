package uk.gov.pay.api.model;

import uk.gov.pay.api.resources.RefundFromConnector;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static black.door.hate.HalRepresentation.*;
import static uk.gov.pay.api.resources.PaymentRefundsResource.*;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_REFUND_BY_ID_PATH;

public class RefundResponse extends HalResourceResponse {

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
