package uk.gov.pay.api.model.search;

import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;

public interface IPaymentSearchPagination {
    int getCount();
    int getTotal();
    int getPage();
    PaymentSearchNavigationLinks getLinks();
}
