package uk.gov.pay.api.service;

public class RefundsParams {
    
    private String fromDate;
    private String toDate;
    private String page;
    private String displaySize;

    public RefundsParams(String fromDate, String toDate, String page, String displaySize) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.page = page;
        this.displaySize = displaySize;
    }

    public String getPage() {
        return page;
    }

    public String getDisplaySize() {
        return displaySize;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }
}
