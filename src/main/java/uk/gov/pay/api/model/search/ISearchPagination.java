package uk.gov.pay.api.model.search;


public interface ISearchPagination<T> {
    int getCount();

    int getTotal();

    int getPage();

    T getLinks();
}
