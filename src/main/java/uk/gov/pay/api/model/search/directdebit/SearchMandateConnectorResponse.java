package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;

import java.util.List;

public class SearchMandateConnectorResponse {
    @JsonProperty("total")
    private int total;
    @JsonProperty("count")
    private int count;
    @JsonProperty("page")
    private int page;
    @JsonProperty("results")
    private List<MandateConnectorResponse> mandates;
    @JsonProperty("_links")
    private SearchNavigationLinks links = new SearchNavigationLinks();

    public int getCount() {
        return count;
    }

    public List<MandateConnectorResponse> getMandates() {
        return mandates;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public SearchNavigationLinks getLinks() {
        return links;
    }


    public static final class SearchMandateConnectorResponseBuilder {
        private int total;
        private int count;
        private int page;
        private List<MandateConnectorResponse> mandates;
        private SearchNavigationLinks links = new SearchNavigationLinks();

        private SearchMandateConnectorResponseBuilder() {
        }

        public static SearchMandateConnectorResponseBuilder aSearchMandateConnectorResponse() {
            return new SearchMandateConnectorResponseBuilder();
        }

        public SearchMandateConnectorResponseBuilder withTotal(int total) {
            this.total = total;
            return this;
        }

        public SearchMandateConnectorResponseBuilder withCount(int count) {
            this.count = count;
            return this;
        }

        public SearchMandateConnectorResponseBuilder withPage(int page) {
            this.page = page;
            return this;
        }

        public SearchMandateConnectorResponseBuilder withMandates(List<MandateConnectorResponse> mandates) {
            this.mandates = List.copyOf(mandates);
            return this;
        }

        public SearchMandateConnectorResponseBuilder withLinks(SearchNavigationLinks links) {
            this.links = links;
            return this;
        }

        public SearchMandateConnectorResponse build() {
            SearchMandateConnectorResponse searchMandateConnectorResponse = new SearchMandateConnectorResponse();
            searchMandateConnectorResponse.mandates = this.mandates;
            searchMandateConnectorResponse.links = this.links;
            searchMandateConnectorResponse.total = this.total;
            searchMandateConnectorResponse.page = this.page;
            searchMandateConnectorResponse.count = this.count;
            return searchMandateConnectorResponse;
        }
    }
}
