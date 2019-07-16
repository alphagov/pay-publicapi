package uk.gov.pay.api.model.search.directdebit;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DirectDebitSearchMandatesParams {

    @QueryParam("reference")
    private String reference;

    @QueryParam("state")
    private String state;

    @QueryParam("bank_statement_reference")
    private String bankStatementReference;

    @QueryParam("email")
    private String email;

    @QueryParam("name")
    private String name;

    @QueryParam("from_date")
    private ZonedDateTime fromDate;

    @QueryParam("to_date")
    private ZonedDateTime toDate;

    @DefaultValue("1")
    @QueryParam("page")
    private int page;

    @DefaultValue("500")
    @QueryParam("display_size")
    private int displaySize;

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

    public Optional<ZonedDateTime> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public Optional<ZonedDateTime> getToDate() {
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
        private ZonedDateTime fromDate;
        private ZonedDateTime toDate;
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

        public DirectDebitSearchMandatesParamsBuilder withFromDate(ZonedDateTime fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public DirectDebitSearchMandatesParamsBuilder withToDate(ZonedDateTime toDate) {
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
