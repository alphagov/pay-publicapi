package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_BY_ID_PATH;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_REFUND_BY_ID_PATH;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RefundResponse {

    private String refundId;
    private String createdDate;
    private Long amount;
    @JsonProperty("_links")
    private RefundLinksForSearch links;
    private String status;

    private RefundResponse(Refund refund, String selfLink, String paymentLink) {
        this.refundId = refund.getRefundId();
        this.amount = refund.getAmount();
        this.status = refund.getStatus();
        this.createdDate = refund.getCreatedDate();
        this.links = new RefundLinksForSearch();

        links.addSelf(selfLink);
        links.addPayment(paymentLink);
    }

    private RefundResponse(String refundId, Long amount, String status,
                           String createdDate, URI selfLink, URI paymentLink) {
        this.refundId = refundId;
        this.amount = amount;
        this.status = status;
        this.createdDate = createdDate;
        this.links = new RefundLinksForSearch();

        links.addSelf(selfLink.toString());
        links.addPayment(paymentLink.toString());
    }

    public static RefundResponse from(Refund refund, String selfLink, String paymentLink) {
        return new RefundResponse(refund, selfLink, paymentLink);
    }

    //todo: remove after full refactoring of PaymentRefundsResource (to use service layer) 
    public static RefundResponse valueOf(RefundFromConnector refundEntity, String paymentId, String baseUrl) {
        URI selfLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_REFUND_BY_ID_PATH)
                .build(paymentId, refundEntity.getRefundId());

        URI paymentLink = UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_BY_ID_PATH)
                .build(paymentId);

        return new RefundResponse(
                refundEntity.getRefundId(),
                refundEntity.getAmount(),
                refundEntity.getStatus(),
                refundEntity.getCreatedDate(),
                selfLink, 
                paymentLink);
    }

    public String getRefundId() {
        return refundId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public RefundLinksForSearch getLinks() {
        return links;
    }
}
