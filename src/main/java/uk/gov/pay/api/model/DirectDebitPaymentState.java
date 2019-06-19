package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class DirectDebitPaymentState extends PaymentState {

    @JsonProperty("details")
    private String details;
    
    public DirectDebitPaymentState(){
        
    }
    
    DirectDebitPaymentState(DirectDebitPaymentStateBuilder builder) {
        super(builder.status, builder.finished);
        this.details = builder.details;
    }
    
    public DirectDebitPaymentState(String status, boolean finished, String details) {
        super(status, finished);
        this.details = details;
    }
    
    @ApiModelProperty(value = "Further information on the state", example = "The payment has been created")
    public String getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DirectDebitPaymentState that = (DirectDebitPaymentState) o;
        return Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), details);
    }

    @Override
    public String toString() {
        return "DirectDebitPaymentState{" +
                "details='" + details + '\'' +
                '}';
    }

    public static final class DirectDebitPaymentStateBuilder {
        private String details;
        private String status;
        private boolean finished;
        private String message;
        private String code;

        private DirectDebitPaymentStateBuilder() {
        }

        public static DirectDebitPaymentStateBuilder aDirectDebitPaymentState() {
            return new DirectDebitPaymentStateBuilder();
        }

        public DirectDebitPaymentStateBuilder withDetails(String details) {
            this.details = details;
            return this;
        }

        public DirectDebitPaymentStateBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public DirectDebitPaymentStateBuilder withFinished(boolean finished) {
            this.finished = finished;
            return this;
        }

        public DirectDebitPaymentStateBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public DirectDebitPaymentStateBuilder withCode(String code) {
            this.code = code;
            return this;
        }

        public DirectDebitPaymentState build() {
            return new DirectDebitPaymentState(status, finished, details);
        }
    }
}
