package uk.gov.pay.api.model.search;

import uk.gov.pay.api.model.links.SearchRefundsNavigationLinks;

public interface ISearchRefundsPagination {
    int getCount();
    int getTotal();
    int getPage();
    SearchRefundsNavigationLinks getLinks();
}
