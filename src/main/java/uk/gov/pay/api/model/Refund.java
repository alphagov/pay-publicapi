package uk.gov.pay.api.model;

public class Refund {

    private String refundId;
    private Long amount;
    private String status;
    private String createdDate;

    private Refund(String refundId, Long amount, String status, String createdDate) {
        this.refundId = refundId;
        this.amount = amount;
        this.status = status;
        this.createdDate = createdDate;
    }

    public static Refund valueOf(RefundFromConnector refundFromConnector) {
        return new Refund(refundFromConnector.getRefundId(),
                refundFromConnector.getAmount(),
                refundFromConnector.getStatus(),
                refundFromConnector.getCreatedDate());
    }

    public static Refund from(String refundId, Long amount, String status, String createdDate) {
        return new Refund(refundId, amount, status, createdDate);
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
}
