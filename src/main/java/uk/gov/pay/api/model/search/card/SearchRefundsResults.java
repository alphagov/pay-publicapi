package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchRefundsResults {

    @JsonProperty(value = "results")
    private List<RefundsForSearchRefundsResult> refunds;

    public SearchRefundsResults(List<RefundsForSearchRefundsResult> refunds) {
        this.refunds = refunds;
    }

    public List<RefundsForSearchRefundsResult> getRefunds() {
        return refunds;
    }
    
    

}
