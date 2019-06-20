package uk.gov.pay.api.resources.directdebit;

import com.fasterxml.jackson.core.JsonProcessingException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.DirectDebitPaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse.DirectDebitConnectorCreatePaymentResponseBuilder.aDirectDebitConnectorCreatePaymentResponse;
import static uk.gov.pay.api.resources.directdebit.DirectDebitPaymentsResourceIT.CreatePaymentRequestValidationParameters.CreatePaymentRequestValidationParametersBuilder.someParameters;

@RunWith(JUnitParamsRunner.class)
public class DirectDebitPaymentsResourceIT extends DirectDebitResourceITBase {
    
    private static final long AMOUNT = 100;
    private static final String REFERENCE = "a reference";
    private static final String DESCRIPTION = "a description";
    private static final String CREATED_DATE = "2018-01-01T11:12:13Z";
    private static final String PAYMENT_ID = "abc123";
    private static final String PROVIDER_ID = "aproviderid";
    private static final String PAYMENT_PROVIDER = "a payment provider";
    private static final String MANDATE_ID = "mandate-123";
    private static final String DETAILS_FIELD = "details";

    @Test
    public void createPayment_success() throws JsonProcessingException {
        final Account account = new Account(GATEWAY_ACCOUNT_ID, TokenPaymentType.DIRECT_DEBIT);
        final String status = "created";
        final boolean finished = false;

        DirectDebitConnectorCreatePaymentResponse connectorResponse = aDirectDebitConnectorCreatePaymentResponse()
                .withPaymentExternalId(PAYMENT_ID)
                .withAmount(AMOUNT)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withCreatedDate(CREATED_DATE)
                .withDescription(DESCRIPTION)
                .withMandateId(MANDATE_ID)
                .withProviderId(PROVIDER_ID)
                .withState(new DirectDebitPaymentState(status, finished, DETAILS_FIELD))
                .withReference(REFERENCE)
                .build();

        connectorDDMockClient.respondWithPaymentCreated(connectorResponse, account.getAccountId());

        Map<String, Object> payload = Map.of(
                "amount", AMOUNT,
                "description", DESCRIPTION,
                "reference", REFERENCE,
                "mandate_id", MANDATE_ID);

        postPaymentResponse(payload)
                .statusCode(201)
                .body("payment_id", is(PAYMENT_ID))
                .body("amount", is((int)AMOUNT))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("description", is(DESCRIPTION))
                .body("reference", is(REFERENCE))
                .body("state.status", is(status))
                .body("state.finished", is(finished))
                .body("_links.events.href", is(paymentEventsLocationFor(PAYMENT_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.self.href", is(paymentLocationFor(PAYMENT_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.mandate.href", is(mandateLocationFor(MANDATE_ID)))
                .body("_links.mandate.method", is("GET"));
    }

    @Test
    public void createPayment_successWhenDescriptionNotPresent() throws JsonProcessingException {
        final Account account = new Account(GATEWAY_ACCOUNT_ID, TokenPaymentType.DIRECT_DEBIT);
        final String status = "created";
        final boolean finished = false;

        DirectDebitConnectorCreatePaymentResponse connectorResponse = aDirectDebitConnectorCreatePaymentResponse()
                .withPaymentExternalId(PAYMENT_ID)
                .withAmount(AMOUNT)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withCreatedDate(CREATED_DATE)
                .withMandateId(MANDATE_ID)
                .withProviderId(PROVIDER_ID)
                .withState(new DirectDebitPaymentState(status, finished, null))
                .withReference(REFERENCE)
                .build();

        connectorDDMockClient.respondWithPaymentCreated(connectorResponse, account.getAccountId());

        Map<String, Object> payload = Map.of(
                "amount", AMOUNT,
                "reference", REFERENCE,
                "mandate_id", MANDATE_ID);

        postPaymentResponse(payload)
                .statusCode(201)
                .body("$", not(hasKey("description")));
    }

    @Test
    @Parameters(method = "parametersForValidation")
    public void validationFailures(CreatePaymentRequestValidationParameters parameters) {
        postPaymentResponse(parameters.toPayload())
                .statusCode(422)
                .contentType(JSON)
                .body("code", is(parameters.expectedErrorCode))
                .body("field", is(parameters.expectedErrorField))
                .body("description", is(parameters.expectedErrorMessage));
    }

    private CreatePaymentRequestValidationParameters[] parametersForValidation() {
        return new CreatePaymentRequestValidationParameters[]{
                someParameters()
                        .withAmount(null)
                        .withErrorMessage("Invalid attribute value: amount. Must be greater than or equal to 1")
                        .withErrorField("amount")
                        .build(),
                someParameters()
                        .withAmount(0L)
                        .withErrorMessage("Invalid attribute value: amount. Must be greater than or equal to 1")
                        .withErrorField("amount")
                        .build(),
                someParameters()
                        .withAmount(10000001L)
                        .withErrorMessage("Invalid attribute value: amount. Must be less than or equal to 10000000")
                        .withErrorField("amount")
                        .build(),
                someParameters()
                        .withAmount("NaN")
                        .withErrorMessage("Invalid attribute value: amount. Must be a valid numeric format")
                        .withErrorField("amount")
                        .build(),
                someParameters()
                        .withAmount(false)
                        .withErrorMessage("Invalid attribute value: amount. Must be a valid numeric format")
                        .withErrorField("amount")
                        .build(),
                someParameters()
                        .withReference(null)
                        .withErrorMessage("Missing mandatory attribute: reference")
                        .withErrorField("reference")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withReference("")
                        .withErrorMessage("Missing mandatory attribute: reference")
                        .withErrorField("reference")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withReference("   ")
                        .withErrorMessage("Missing mandatory attribute: reference")
                        .withErrorField("reference")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withReference(RandomStringUtils.randomAlphanumeric(256))
                        .withErrorMessage("Invalid attribute value: reference. Must be less than or equal to 255 characters length")
                        .withErrorField("reference")
                        .withErrorCode("P0102")
                        .build(),
                someParameters()
                        .withReference(123)
                        .withErrorMessage("Invalid attribute value: reference. Must be of type String")
                        .withErrorField("reference")
                        .build(),
                someParameters()
                        .withReference(false)
                        .withErrorMessage("Invalid attribute value: reference. Must be of type String")
                        .withErrorField("reference")
                        .build(),
                someParameters()
                        .withDescription(RandomStringUtils.randomAlphanumeric(256))
                        .withErrorMessage("Invalid attribute value: description. Must be less than or equal to 255 characters length")
                        .withErrorField("description")
                        .withErrorCode("P0102")
                        .build(),
                someParameters()
                        .withDescription(123)
                        .withErrorMessage("Invalid attribute value: description. Must be of type String")
                        .withErrorField("description")
                        .build(),
                someParameters()
                        .withDescription(false)
                        .withErrorMessage("Invalid attribute value: description. Must be of type String")
                        .withErrorField("description")
                        .build(),
                someParameters()
                        .withMandateId(null)
                        .withErrorMessage("Missing mandatory attribute: mandate_id")
                        .withErrorField("mandate_id")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withMandateId("")
                        .withErrorMessage("Missing mandatory attribute: mandate_id")
                        .withErrorField("mandate_id")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withMandateId("   ")
                        .withErrorMessage("Missing mandatory attribute: mandate_id")
                        .withErrorField("mandate_id")
                        .withErrorCode("P0101")
                        .build(),
                someParameters()
                        .withMandateId(RandomStringUtils.randomAlphanumeric(27))
                        .withErrorMessage("Invalid attribute value: mandate_id. Must be less than or equal to 26 characters length")
                        .withErrorField("mandate_id")
                        .withErrorCode("P0102")
                        .build(),
                someParameters()
                        .withMandateId(123)
                        .withErrorMessage("Invalid attribute value: mandate_id. Must be of type String")
                        .withErrorField("mandate_id")
                        .build(),
                someParameters()
                        .withMandateId(false)
                        .withErrorMessage("Invalid attribute value: mandate_id. Must be of type String")
                        .withErrorField("mandate_id")
                        .build(),
        };
    }
    
    public static class CreatePaymentRequestValidationParameters {
        public Object amount;
        public Object reference;
        public Object description;
        public Object mandateId;
        public String expectedErrorCode;
        public String expectedErrorField;
        public String expectedErrorMessage;

        CreatePaymentRequestValidationParameters(CreatePaymentRequestValidationParametersBuilder builder) {
            this.amount = builder.amount;
            this.reference = builder.reference;
            this.description = builder.description;
            this.mandateId = builder.mandateId;
            this.expectedErrorCode = builder.expectedErrorCode;
            this.expectedErrorField = builder.expectedErrorField;
            this.expectedErrorMessage = builder.expectedErrorMessage;
        }

        Map<String, Object> toPayload() {
            var payload = new HashMap<String, Object>();
            Optional.ofNullable(amount).ifPresent(ref -> payload.put("amount", ref));
            Optional.ofNullable(reference).ifPresent(ref -> payload.put("reference", ref));
            Optional.ofNullable(description).ifPresent(ref -> payload.put("description", ref));
            Optional.ofNullable(mandateId).ifPresent(ref -> payload.put("mandate_id", ref));
            return payload;
        }

        @Override
        public String toString() {
            return "CreatePaymentRequestValidationParameters{" +
                    "amount=" + amount +
                    ", reference=" + reference +
                    ", description=" + description +
                    ", mandateId=" + mandateId +
                    ", expectedErrorCode='" + expectedErrorCode + '\'' +
                    ", expectedErrorField='" + expectedErrorField + '\'' +
                    ", expectedErrorMessage='" + expectedErrorMessage + '\'' +
                    '}';
        }

        static class  CreatePaymentRequestValidationParametersBuilder {
            public Object amount = AMOUNT;
            public Object reference = REFERENCE;
            public Object description = DESCRIPTION;
            public Object mandateId = MANDATE_ID;
            public String expectedErrorCode = "P0102";
            public String expectedErrorField;
            public String expectedErrorMessage;

            static CreatePaymentRequestValidationParametersBuilder someParameters() {
                return new CreatePaymentRequestValidationParametersBuilder();
            }

            CreatePaymentRequestValidationParametersBuilder withAmount(Object amount) {
                this.amount = amount;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withReference(Object reference) {
                this.reference = reference;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withDescription(Object description) {
                this.description = description;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withMandateId(Object mandateId) {
                this.mandateId = mandateId;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorCode(String errorCode) {
                this.expectedErrorCode= errorCode;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorField(String errorField) {
                this.expectedErrorField= errorField;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorMessage(String errorMessage) {
                this.expectedErrorMessage= errorMessage;
                return this;
            }

            CreatePaymentRequestValidationParameters build() {
                return new CreatePaymentRequestValidationParameters(this);
            }
        }
    }
}
