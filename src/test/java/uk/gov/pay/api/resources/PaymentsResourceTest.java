package uk.gov.pay.api.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.Wallet;
import uk.gov.pay.api.service.ConnectorService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.LedgerService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import java.util.Collections;
import java.util.Map;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentsResourceTest {

    private static final Account SOME_ACCOUNT = new Account(null, null, null);
    private static final String SOME_PAYMENT_ID = "123";
    private static final String LEDGER_ONLY_STRATEGY = "ledger-only";
    public static final String DEFAULT_STRATEGY = null;
    @Mock
    private ConnectorService connectorServiceStub;
    @Mock
    private LedgerService ledgerServiceStub;
    private static final ObjectMapper MAPPER = newObjectMapper();
    private PaymentsResource paymentsResource;


    @BeforeEach
    void setUp() {
        var publicApiUriGenerator = new PublicApiUriGenerator(new TestPublicApiConfig());
        var getPaymentService = new GetPaymentService(publicApiUriGenerator, connectorServiceStub, ledgerServiceStub);
        paymentsResource = new PaymentsResource(null, null, null, getPaymentService, null, null, null);
    }

    @Test
    void shouldReturnWalletTypeWithinCardDetailsProperty() throws JsonProcessingException {
        var cardDetails = aCardDetails().withWalletType(Wallet.GOOGLE_PAY).build();
        var charge = aCharge()
                .withCardDetails(cardDetails)
                .build();
        given(connectorServiceStub.getCharge(SOME_ACCOUNT, SOME_PAYMENT_ID)).willReturn(charge);

        var payment = paymentsResource.getPayment(SOME_ACCOUNT, SOME_PAYMENT_ID, DEFAULT_STRATEGY);

        assertThat(propertyAtPath(payment, "card_details.wallet_type"), is("Google Pay"));
    }

    @Test
    void shouldReturnMetadataFromConnector() throws JsonProcessingException {
        var charge = aCharge()
                .withMetadata(Map.of(
                        "reconciled", true,
                        "ledger_code", 123,
                        "surcharge", 1.23))
                .build();
        given(connectorServiceStub.getCharge(SOME_ACCOUNT, SOME_PAYMENT_ID))
                .willReturn(charge);

        var payment = paymentsResource.getPayment(SOME_ACCOUNT, SOME_PAYMENT_ID, DEFAULT_STRATEGY);

        assertThat(propertyAtPath(payment, "metadata.reconciled"), is(true));
        assertThat(propertyAtPath(payment, "metadata.ledger_code"), is(123));
        assertThat(propertyAtPath(payment, "metadata.surcharge"), is(1.23));
    }

    @Test
    void shouldReturnMetadataFromLedger() throws JsonProcessingException {
        var charge = aCharge()
                .withMetadata(Map.of(
                        "reconciled", true,
                        "ledger_code", 123,
                        "surcharge", 1.23))
                .build();
        given(ledgerServiceStub.getPaymentTransaction(SOME_ACCOUNT, SOME_PAYMENT_ID))
                .willReturn(charge);

        var payment = paymentsResource.getPayment(SOME_ACCOUNT, SOME_PAYMENT_ID, LEDGER_ONLY_STRATEGY);

        assertThat(propertyAtPath(payment, "metadata.reconciled"), is(true));
        assertThat(propertyAtPath(payment, "metadata.ledger_code"), is(123));
        assertThat(propertyAtPath(payment, "metadata.surcharge"), is(1.23));
    }

    @Test
    void shouldReturnMotoValueFromConnector() throws JsonProcessingException {
        var charge = aCharge()
                .withMotoTrue()
                .build();
        given(connectorServiceStub.getCharge(SOME_ACCOUNT, SOME_PAYMENT_ID))
                .willReturn(charge);

        var payment = paymentsResource.getPayment(SOME_ACCOUNT, SOME_PAYMENT_ID, DEFAULT_STRATEGY);

        assertThat(propertyAtPath(payment, "moto"), is(true));
    }

    @Test
    void shouldReturnMotoValueFromLedger() throws JsonProcessingException {
        var charge = aCharge()
                .withMotoTrue()
                .build();
        given(ledgerServiceStub.getPaymentTransaction(SOME_ACCOUNT, SOME_PAYMENT_ID))
                .willReturn(charge);

        var payment = paymentsResource.getPayment(SOME_ACCOUNT, SOME_PAYMENT_ID, LEDGER_ONLY_STRATEGY);

        assertThat(propertyAtPath(payment, "moto"), is(true));
    }

    private static <T> T propertyAtPath(Response payment, String jsonPath) throws JsonProcessingException {
        String readPayment = MAPPER.writeValueAsString(payment.getEntity());
        return JsonPath.read(readPayment, jsonPath);
    }

    private CardDetailsBuilder aCardDetails() {
        return new CardDetailsBuilder();
    }

    private ChargeBuilder aCharge() {
        return new ChargeBuilder();
    }

    private static class TestPublicApiConfig extends PublicApiConfig {
        @Override
        public String getConnectorUrl() {
            return "https://example.com";
        }

        @Override
        public String getBaseUrl() {
            return "https://example.com";
        }
    }

    private static class ChargeBuilder {
        private CardDetails cardDetails;
        private ExternalMetadata metaData;
        private boolean moto;

        public ChargeBuilder withCardDetails(CardDetails cardDetails) {
            this.cardDetails = cardDetails;
            return this;
        }

        public ChargeBuilder withMetadata(Map<String, Object> metaData) {
            this.metaData = new ExternalMetadata(metaData);
            return this;
        }

        public ChargeBuilder withMotoTrue() {
            this.moto = true;
            return this;
        }

        public Charge build() {
            return new Charge("123", 1234L, new PaymentState(), null, null, null, null, null, null, SupportedLanguage.ENGLISH, false, moto, null, null, this.cardDetails, Collections.emptyList(), null, null, null, metaData, null, null, null, null, null, null, null);
        }
    }


    private static class CardDetailsBuilder {
        private Wallet walletType;

        public CardDetails build() {
            return new CardDetails("4444", "2222", "J Smith", null, null, null, null, walletType.getTitleCase());
        }

        public CardDetailsBuilder withWalletType(Wallet walletType) {
            this.walletType = walletType;
            return this;
        }
    }
}
 
