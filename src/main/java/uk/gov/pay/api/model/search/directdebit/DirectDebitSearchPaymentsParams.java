package uk.gov.pay.api.model.search.directdebit;

import io.swagger.annotations.ApiParam;
import uk.gov.pay.commons.validation.ValidDate;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.QueryParam;

import java.util.Optional;

import static uk.gov.pay.api.model.CreateDirectDebitPaymentRequest.MANDATE_ID_MAX_LENGTH;
import static uk.gov.pay.api.model.CreateDirectDebitPaymentRequest.REFERENCE_MAX_LENGTH;

public class DirectDebitSearchPaymentsParams {

    @QueryParam("reference")
    @ApiParam(value = "The payment reference to search for - case insensitive.")
    @Size(min = 1, max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;

    @QueryParam("state")
    @ApiParam(value = "The payment state to search for - case sensitive.")
    @Pattern(regexp = "created|pending|success|failed|cancelled|paidout|indemnityclaim|error",
            message = "Must be one of created, pending, success, failed, cancelled, paidout, indemnityclaim or error")
    private String state;

    @QueryParam("mandate_id")
    @ApiParam(value = "The mandate identifier to search for - case insensitive.")
    @Size(min = 1, max = MANDATE_ID_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String mandateId;

    @QueryParam("from_date")
    @ApiParam(value = "The start date for payments to be searched - inclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.")
    @ValidDate
    private String fromDate;

    @QueryParam("to_date")
    @ApiParam(value = "The end date for payments to be searched - exclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.")
    @ValidDate
    private String toDate;

    @QueryParam("page")
    @ApiParam(value = "The page number of results to return.")
    @Min(value = 1, message = "Must be greater than or equal to {value}")
    private Integer page;

    @QueryParam("display_size")
    @ApiParam(value = "The number of results per page.")
    @Min(value = 1, message = "Must be greater than or equal to {value}")
    @Max(value = 500, message = "Must be less than or equal to {value}")
    private Integer displaySize;

    public String getReference() {
        return reference;
    }

    public String getState() {
        return state;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public Optional<Integer> getPage() {
        return Optional.ofNullable(page);
    }

    public Optional<Integer> getDisplaySize() {
        return Optional.ofNullable(displaySize);
    }

    @Override
    public String toString() {
        return "DirectDebitSearchPaymentsParams{" +
                "reference='" + reference + '\'' +
                ", state='" + state + '\'' +
                ", mandateId='" + mandateId + '\'' +
                ", fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", page='" + page + '\'' +
                ", displaySize='" + displaySize + '\'' +
                '}';
    }


    public static final class DirectDebitSearchPaymentsParamsBuilder {
        private String reference;
        private String state;
        private String mandateId;
        private String fromDate;
        private String toDate;
        private Integer page;
        private Integer displaySize;

        private DirectDebitSearchPaymentsParamsBuilder() {
        }

        public static DirectDebitSearchPaymentsParamsBuilder aDirectDebitSearchPaymentsParams() {
            return new DirectDebitSearchPaymentsParamsBuilder();
        }

        public DirectDebitSearchPaymentsParamsBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withState(String state) {
            this.state = state;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withFromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withToDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withPage(Integer page) {
            this.page = page;
            return this;
        }

        public DirectDebitSearchPaymentsParamsBuilder withDisplaySize(Integer displaySize) {
            this.displaySize = displaySize;
            return this;
        }

        public DirectDebitSearchPaymentsParams build() {
            DirectDebitSearchPaymentsParams directDebitSearchPaymentsParams = new DirectDebitSearchPaymentsParams();
            directDebitSearchPaymentsParams.mandateId = this.mandateId;
            directDebitSearchPaymentsParams.displaySize = this.displaySize;
            directDebitSearchPaymentsParams.reference = this.reference;
            directDebitSearchPaymentsParams.state = this.state;
            directDebitSearchPaymentsParams.toDate = this.toDate;
            directDebitSearchPaymentsParams.fromDate = this.fromDate;
            directDebitSearchPaymentsParams.page = this.page;
            return directDebitSearchPaymentsParams;
        }
    }
}
