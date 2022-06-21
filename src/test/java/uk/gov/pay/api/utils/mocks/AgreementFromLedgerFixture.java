package uk.gov.pay.api.utils.mocks;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.agreement.model.Agreement;

import java.time.ZonedDateTime;

import static uk.gov.service.payments.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AgreementFromLedgerFixture {
    // this should be used to generate pacts with Ledger
    private final String externalId;
    private final String serviceId;
    private final String reference;
    private final String description;
    private final String status;
    private final String createdDate;
    private final Agreement.PaymentInstrument paymentInstrument;

    private AgreementFromLedgerFixture(AgreementFromLedgerFixtureBuilder builder) {
        this.externalId = builder.externalId;
        this.serviceId = builder.serviceId;
        this.reference = builder.reference;
        this.description = builder.description;
        this.status = builder.status;
        this.createdDate = builder.createdDate;
        this.paymentInstrument = builder.paymentInstrument;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public Agreement.PaymentInstrument getPaymentInstrument() {
        return paymentInstrument;
    }

    public static final class AgreementFromLedgerFixtureBuilder {
        private String externalId = "an-external-id";
        private String serviceId = "a-service-id";
        private String reference = "valid-reference";
        private String description = "An agreement description";
        private String status = "CREATED";
        private String createdDate = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.parse("2022-07-20T11:01:00.132012345Z"));
        private Agreement.PaymentInstrument paymentInstrument;

        private AgreementFromLedgerFixtureBuilder() {
        }

        public static AgreementFromLedgerFixtureBuilder anAgreementFromLedgerFixture() {
            return new AgreementFromLedgerFixtureBuilder();
        }

        public AgreementFromLedgerFixtureBuilder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public AgreementFromLedgerFixtureBuilder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public AgreementFromLedgerFixtureBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public AgreementFromLedgerFixtureBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public AgreementFromLedgerFixtureBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public AgreementFromLedgerFixtureBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }
        
        public AgreementFromLedgerFixtureBuilder withPaymentInstrument(Agreement.PaymentInstrument paymentInstrument) {
            this.paymentInstrument = paymentInstrument;
            return this;
        }
        
        public AgreementFromLedgerFixture build() {
            return new AgreementFromLedgerFixture(this);
        }
    }
}
