package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SearchNavigationLinks", description = "Links to navigate through pages")
public class SearchNavigationLinks {

    @JsonProperty(value = "self")
    private Link self;

    @JsonProperty(value = "first_page")
    private Link firstPage;

    @JsonProperty(value = "last_page")
    private Link lastPage;

    @JsonProperty(value = "prev_page")
    private Link prevPage;

    @JsonProperty(value = "next_page")
    private Link nextPage;

    public Link getSelf() {
        return self;
    }

    public Link getFirstPage() {
        return firstPage;
    }

    public Link getLastPage() {
        return lastPage;
    }

    public Link getPrevPage() {
        return prevPage;
    }

    public Link getNextPage() {
        return nextPage;
    }

    public SearchNavigationLinks withSelfLink(String href) {
        this.self = new Link(href);
        return this;
    }
    public SearchNavigationLinks withPrevLink(String href) {
        this.prevPage = new Link(href);
        return this;
    }
    public SearchNavigationLinks withNextLink(String href) {
        this.nextPage = new Link(href);
        return this;
    }
    public SearchNavigationLinks withFirstLink(String href) {
        this.firstPage = new Link(href);
        return this;
    }
    public SearchNavigationLinks withLastLink(String href) {
        this.lastPage = new Link(href);
        return this;
    }
}
