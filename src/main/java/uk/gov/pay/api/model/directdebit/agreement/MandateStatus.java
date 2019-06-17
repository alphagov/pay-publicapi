package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MandateStatus {
    CREATED("created");
    
    private String status;

    MandateStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
