package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MandateStatus {
    CREATED("created"),
    STARTED("started"),
    PENDING("pending"),
    SUBMITTED("submitted"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    CANCELLED("cancelled");
    
    private String status;

    MandateStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
