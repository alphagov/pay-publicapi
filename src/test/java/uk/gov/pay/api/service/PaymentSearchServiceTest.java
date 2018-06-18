package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;

import javax.ws.rs.client.Client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSearchServiceTest {

    @Mock
    private PaymentSearchService paymentSearchService;
    @Mock
    private Client client;
    @Mock
    private PublicApiConfig configuration;
    private ConnectorUriGenerator connectorUriGenerator;
    private PaymentUriGenerator paymentUriGenerator;
    private ObjectMapper objectMapper;
    @Before
    public void setUp() {
        connectorUriGenerator = new ConnectorUriGenerator(configuration);
        paymentUriGenerator = new PaymentUriGenerator();
        objectMapper = new ObjectMapper();
        paymentSearchService = new PaymentSearchService(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
    }
    
    @Test
    public void doSearchShouldThrowBadRequestException_whenAccountIsNotDD_andAgreementIsASearchParam() {
        Account account = new Account("an account", TokenPaymentType.CARD);
        String agreementId = "an-agreement-id";
        try {
            paymentSearchService.doSearch(account, null, null, null, null, null,
                    null, null, null, agreementId);
        } catch (uk.gov.pay.api.exception.BadRequestException ex) {
            assertThat(ex.getPaymentError().getCode(), is("P0401"));
            assertThat(ex.getPaymentError().getDescription().contains("Invalid parameters: agreement."), is(true));
        }
    }
}
