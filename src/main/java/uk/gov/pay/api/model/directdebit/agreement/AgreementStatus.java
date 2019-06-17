package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AgreementStatus {
    CREATED("created");
    
    private String status;

    AgreementStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
