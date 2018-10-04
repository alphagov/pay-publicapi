package uk.gov.pay.api.model.search.links;

import uk.gov.pay.api.model.generated.Link;

import static javax.ws.rs.HttpMethod.GET;

public class DDTransactionLinksForSearch {
    private Link self;

    public void addSelf(String href) { this.self = new Link().href(href).method(GET); }

    public Link getSelf() { return self; }
}
