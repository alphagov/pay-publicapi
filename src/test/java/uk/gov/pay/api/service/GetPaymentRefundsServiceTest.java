package uk.gov.pay.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.Refund;
import uk.gov.pay.api.model.RefundResponse;
import uk.gov.pay.api.model.RefundsResponse;
import uk.gov.pay.api.model.TokenPaymentType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GetPaymentRefundsServiceTest {

    public static final String BASE_URL = "http://publicapi.test.localhost/";
    private static final String ACCOUNT_ID = "123456";
    private static final String PAYMENT_ID = "ch_ablppdowdlxvlq";

    @Mock
    private ConnectorService mockConnectorService;
    @Mock
    private PublicApiConfig mockConfiguration;

    private Account account;
    private GetPaymentRefundsService getPaymentRefundsService;

    @Before
    public void setup() {

        when(mockConfiguration.getBaseUrl()).thenReturn(BASE_URL);

        getPaymentRefundsService = new GetPaymentRefundsService(mockConnectorService,
                mockConfiguration);
        account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
    }

    @Test
    public void shouldGetRefundResponseCorrectlyForRefundsFromConnector() {

        String refundId = "refundza1xsdasd";
        Refund refund = Refund.from(refundId,
                1000L,
                "created",
                "2017-01-10T16:52:07.855Z");
        when(mockConnectorService.getPaymentRefunds(ACCOUNT_ID, PAYMENT_ID))
                .thenReturn(List.of(refund));

        RefundsResponse refundsResponse = getPaymentRefundsService.getConnectorPaymentRefunds(account, PAYMENT_ID);

        assertThat(refundsResponse.getPaymentId(), is(PAYMENT_ID));
        assertThat(refundsResponse.getLinks().getSelf().getHref(), is(BASE_URL + "v1/payments/" + PAYMENT_ID + "/refunds"));
        assertThat(refundsResponse.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(refundsResponse.getLinks().getPayment().getHref(), is(BASE_URL + "v1/payments/" + PAYMENT_ID));
        assertThat(refundsResponse.getLinks().getPayment().getMethod(), is("GET"));

        RefundResponse refundResponse = refundsResponse.getEmbedded().getRefunds().get(0);
        assertThat(refundResponse.getRefundId(), is(refundId));
        assertThat(refundResponse.getAmount(), is(1000L));
        assertThat(refundResponse.getStatus(), is("created"));
        assertThat(refundResponse.getCreatedDate(), is("2017-01-10T16:52:07.855Z"));
        assertThat(refundResponse.getLinks().getSelf().getHref(), is(BASE_URL + "v1/payments/" + PAYMENT_ID + "/refunds/" + refundId));
        assertThat(refundResponse.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(refundResponse.getLinks().getPayment().getHref(), is(BASE_URL + "v1/payments/" + PAYMENT_ID));
        assertThat(refundResponse.getLinks().getPayment().getMethod(), is("GET"));
    }
}
