package uk.gov.pay.api.model.directdebit;

import uk.gov.pay.api.model.response.Link;

import static javax.ws.rs.HttpMethod.GET;

public class DDTransactionLinksForSearch {
    private Link self;

    public void addSelf(String href) { this.self = new Link(href, GET); }

    public Link getSelf() { return self; }
}
