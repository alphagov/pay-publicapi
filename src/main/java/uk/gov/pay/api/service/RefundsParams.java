package uk.gov.pay.api.service;

public class RefundsParams {

    private String page;
    private String displaySize;

    public RefundsParams(String page, String displaySize) {
        this.page = page;
        this.displaySize = displaySize;
    }

    public String getPage() {
        return page;
    }

    public String getDisplaySize() {
        return displaySize;
    }
}
