package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

import java.util.List;

public class RefundsResponse {

    @JsonProperty("payment_id")
    private String paymentId;
    @JsonProperty("_links")
    private RefundLinksForSearch links;
    @JsonProperty("_embedded")
    private EmbeddedRefunds embedded;

    private RefundsResponse(String paymentId, List<RefundResponse> refundsForPayment, String selfLink, String paymentLink) {
        this.paymentId = paymentId;

        embedded = new EmbeddedRefunds();
        embedded.refunds = refundsForPayment;

        this.links = new RefundLinksForSearch();
        this.links.addPayment(paymentLink);
        this.links.addSelf(selfLink);
    }

    public static RefundsResponse from(String paymentId,
                                     List<RefundResponse> refundsForPayment,
                                     String selfLink,
                                     String paymentLink) {
        return new RefundsResponse(paymentId, refundsForPayment, selfLink, paymentLink);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public RefundLinksForSearch getLinks() {
        return links;
    }

    public EmbeddedRefunds getEmbedded() {
        return embedded;
    }

    public class EmbeddedRefunds {
        private List<RefundResponse> refunds;

        public EmbeddedRefunds() {
        }

        public List<RefundResponse> getRefunds() {
            return refunds;
        }

        @Override
        public String toString() {
            return "Embedded{" +
                    "refunds=" + refunds +
                    '}';
        }
    }
}
