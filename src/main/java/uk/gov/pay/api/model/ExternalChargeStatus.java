package uk.gov.pay.api.model;


import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.upperCase;

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
        String upperCasedStatus = upperCase(statusValue);
        for (ExternalChargeStatus chargeStatus : values()) {
            if (chargeStatus.status.equals(upperCasedStatus)) {
                return Optional.of(chargeStatus);
            }
        }
        return Optional.empty();
    }
}
