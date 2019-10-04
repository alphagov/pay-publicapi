package uk.gov.pay.api.model.search.directdebit;

import io.swagger.annotations.ApiParam;
import uk.gov.pay.commons.validation.ValidDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DirectDebitSearchMandatesParams {

    @QueryParam("reference")
    private String reference;

    @QueryParam("state")
    @Pattern(regexp = "created|started|pending|active|inactive|cancelled|failed|abandoned|error",
            message = "Must be one of created, started, pending, active, inactive, cancelled, failed, abandoned or error")
    private String state;

    @QueryParam("bank_statement_reference")
    private String bankStatementReference;

    @QueryParam("email")
    private String email;

    @QueryParam("name")
    private String name;

    @ApiParam(value = "The start date for mandates to be searched - inclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.")
    @QueryParam("from_date")
    @ValidDate()
    private String fromDate;

    @QueryParam("to_date")
    @ApiParam(value = "The end date for mandates to be searched - exclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.")
    @ValidDate()
    private String toDate;

    @QueryParam("page")
    @ApiParam(value = "Which page number of results to return.")
    @DefaultValue("1")
    @Min(value = 1, message = "Must be greater than or equal to {value}")
    private int page;

    @QueryParam("display_size")
    @ApiParam(value = "The number of results per page.")
    @DefaultValue("500")
    @Min(value = 1, message = "Must be greater than or equal to {value}")
    @Max(value = 500, message = "Must be less than or equal to {value}")
    private int displaySize;

    public DirectDebitSearchMandatesParams() { };

    private DirectDebitSearchMandatesParams(DirectDebitSearchMandatesParamsBuilder builder) {
        this.reference = builder.reference;
        this.state = builder.state;
        this.bankStatementReference = builder.bankStatementReference;
        this.email = builder.email;
        this.name = builder.name;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.page = builder.page;
        this.displaySize = builder.displaySize;
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public Optional<String> getState() {
        return Optional.ofNullable(state);
    }

    public Optional<String> getBankStatementReference() {
        return Optional.ofNullable(bankStatementReference);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public Optional<String> getToDate() {
        return Optional.ofNullable(toDate);
    }

    public int getPage() {
        return page;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public Map<String, String> paramsAsMap() {
        var params = new HashMap<String, String>();

        params.put("display_size", String.valueOf(this.getDisplaySize()));
        params.put("page", String.valueOf(this.getPage()));

        this.getBankStatementReference().ifPresent(bankStatementReference ->
                params.put("bank_statement_reference", bankStatementReference));
        this.getEmail().ifPresent(email -> params.put("email", email));
        this.getName().ifPresent(name -> params.put("name", name));
        this.getFromDate().ifPresent(fromDate -> params.put("from_date", String.valueOf(fromDate)));
        this.getToDate().ifPresent(toDate -> params.put("to_date", String.valueOf(toDate)));
        this.getReference().ifPresent(reference -> params.put("reference", reference));
        this.getState().ifPresent(state -> params.put("state", state));

        return Collections.unmodifiableMap(params);
    }

    public static final class DirectDebitSearchMandatesParamsBuilder {
        private String reference;
        private String state;
        private String bankStatementReference;
        private String email;
        private String name;
        private String fromDate;
        private String toDate;
        private int page = 1;
        private int displaySize = 500;

        private DirectDebitSearchMandatesParamsBuilder() {
        }

        public static DirectDebitSearchMandatesParamsBuilder aDirectDebitSearchMandatesParams() {
            return new DirectDebitSearchMandatesParamsBuilder();
        }

        public DirectDebitSearchMandatesParamsBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withState(String state) {
            this.state = state;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withBankStatementReference(String bankStatementReference) {
            this.bankStatementReference = bankStatementReference;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withFromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withToDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withPage(int page) {
            this.page = page;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withDisplaySize(int displaySize) {
            this.displaySize = displaySize;
            return this;
        }

        public DirectDebitSearchMandatesParams build() {
            return new DirectDebitSearchMandatesParams(this);
        }
    }
}
