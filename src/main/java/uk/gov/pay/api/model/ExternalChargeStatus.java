package uk.gov.pay.api.model;


import java.util.Optional;

public enum ExternalChargeStatus {
    EXT_CREATED("CREATED"),
    EXT_IN_PROGRESS("IN PROGRESS"),
    EXT_SUCCEEDED("SUCCEEDED"),
    EXT_FAILED("FAILED"),
    EXT_SYSTEM_CANCELLED("SYSTEM CANCELLED");

    private String status;

    ExternalChargeStatus(String status) {
        this.status = status;
    }

    public static Optional<ExternalChargeStatus> mapFromStatus(String statusValue) {
        for (ExternalChargeStatus chargeStatus : values()) {
            if (chargeStatus.status.equals(statusValue)) {
                return Optional.of(chargeStatus);
            }
        }
        return Optional.empty();
    }
}
