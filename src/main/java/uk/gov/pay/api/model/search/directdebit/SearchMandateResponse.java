package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

public class SearchMandateResponse {
    @JsonProperty("total")
    @Schema(accessMode = READ_ONLY)
    private final int total;
    @JsonProperty("count")
    @Schema(accessMode = READ_ONLY)
    private final int count;
    @JsonProperty("page")
    @Schema(accessMode = READ_ONLY)
    private final int page;
    @JsonProperty("results")
    @Schema(accessMode = READ_ONLY)
    private final List<MandateResponse> mandates;
    @JsonProperty("_links")
    private final SearchNavigationLinks links;

    private SearchMandateResponse(SearchMandateResponseBuilder builder) {
        this.total = builder.total;
        this.count = builder.count;
        this.page = builder.page;
        this.mandates = builder.mandates;
        this.links = builder.links;
    }

    public int getCount() {
        return count;
    }

    public List<MandateResponse> getMandates() {
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

    public static final class SearchMandateResponseBuilder {
        private int total;
        private int count;
        private int page;
        private List<MandateResponse> mandates;
        private SearchNavigationLinks links = new SearchNavigationLinks();

        private SearchMandateResponseBuilder() {
        }

        public static SearchMandateResponseBuilder aSearchMandateResponse() {
            return new SearchMandateResponseBuilder();
        }

        public SearchMandateResponseBuilder withTotal(int total) {
            this.total = total;
            return this;
        }

        public SearchMandateResponseBuilder withCount(int count) {
            this.count = count;
            return this;
        }

        public SearchMandateResponseBuilder withPage(int page) {
            this.page = page;
            return this;
        }

        public SearchMandateResponseBuilder withMandates(List<MandateResponse> mandates) {
            this.mandates = List.copyOf(mandates);
            return this;
        }

        public SearchMandateResponseBuilder withLinks(SearchNavigationLinks links) {
            this.links = links;
            return this;
        }

        public SearchMandateResponse build() {
            return new SearchMandateResponse(this);
        }
    }
}
