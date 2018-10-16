package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchRefundsResults {

    @JsonProperty("results")
    private List<RefundForSearchRefundsResult> refunds;

    public SearchRefundsResults(List<RefundForSearchRefundsResult> refunds) {
        this.refunds = refunds;
    }

    public List<RefundForSearchRefundsResult> getRefunds() {
        return refunds;
    }
    
    

}
