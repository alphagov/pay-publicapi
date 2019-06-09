package uk.gov.pay.api.model.card;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectorPaymentState {
    public String status;
    public boolean finished;
    public String message;
    public String code;

    public ConnectorPaymentState() {
    }

    public ConnectorPaymentState(String status, boolean finished, String message, String code) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
    }
}
