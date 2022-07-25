package uk.gov.pay.api.ledger.model;

import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.pay.api.common.SearchConstants.DISPLAY_SIZE;
import static uk.gov.pay.api.common.SearchConstants.PAGE;
import static uk.gov.pay.api.common.SearchConstants.REFERENCE_KEY;
import static uk.gov.pay.api.common.SearchConstants.STATUS_KEY;

public class AgreementSearchParams {
    @QueryParam("reference")
    private String reference;

    @QueryParam("status")
    private String status;

    @QueryParam("page")
    private String pageNumber;

    @QueryParam("display_size")
    private String displaySize;

    public AgreementSearchParams(String reference, String status, String pageNumber, String displaySize) {
        this.reference = reference;
        this.status = status;
        this.pageNumber = pageNumber;
        this.displaySize = displaySize;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(String displaySize) {
        this.displaySize = displaySize;
    }

    public Map<String, String> getQueryMap() {
        var queryParams = new HashMap<String, String>();
        Optional.ofNullable(reference).ifPresent(reference -> queryParams.put(REFERENCE_KEY, reference));
        Optional.ofNullable(status).ifPresent(status -> queryParams.put(STATUS_KEY, status));
        Optional.ofNullable(pageNumber).ifPresent(pageNumber -> queryParams.put(PAGE, pageNumber));
        Optional.ofNullable(displaySize).ifPresent(displaySize -> queryParams.put(DISPLAY_SIZE, displaySize));
        return Map.copyOf(queryParams);
    }
}
