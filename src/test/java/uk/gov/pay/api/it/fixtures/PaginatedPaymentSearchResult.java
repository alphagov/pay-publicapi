package uk.gov.pay.api.it.fixtures;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;

import java.util.List;

public class PaginatedPaymentSearchResult {

    private int total;
    private int count;
    private int page;
    private List results;
    private PaymentSearchNavigationLinks _links;

    public static PaginatedPaymentSearchResult aPaginatedPaymentSearchResult() {
        return new PaginatedPaymentSearchResult();
    }

    public PaginatedPaymentSearchResult withTotal(int total) {
        this.total = total;
        return this;
    }

    public PaginatedPaymentSearchResult withCount(int count) {
        this.count = count;
        return this;
    }

    public PaginatedPaymentSearchResult withPage(int page) {
        this.page = page;
        return this;
    }

    public PaginatedPaymentSearchResult withLinks(PaymentSearchNavigationLinks links) {
        this._links = links;
        return this;
    }

    public PaginatedPaymentSearchResult withPayments(List results) {
        this.results = results;
        return this;
    }

    public String build() {
        String json = new Gson().toJson(this, new TypeReference<PaginatedPaymentSearchResult>() {
        }.getType());
        return json;
    }
}
