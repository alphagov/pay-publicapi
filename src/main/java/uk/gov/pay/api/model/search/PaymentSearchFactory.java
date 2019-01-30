package uk.gov.pay.api.model.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.card.SearchCardPayments;
import uk.gov.pay.api.model.search.directdebit.SearchDirectDebitPayments;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;

import static java.lang.String.format;

public class PaymentSearchFactory {
    
    public static SearchPaymentsBase getPaymentService(Account account,
                                                       Client client,
                                                       PublicApiConfig configuration,
                                                       ConnectorUriGenerator connectorUriGenerator,
                                                       PaymentUriGenerator paymentUriGenerator,
                                                       ObjectMapper objectMapper) {
        switch (account.getPaymentType()) {
            case CARD:
                return new SearchCardPayments(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
            case DIRECT_DEBIT:
                return new SearchDirectDebitPayments(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
            default: throw new WebApplicationException(format("Unrecognised payment type: %s", account.getPaymentType()));
        }
    }
}
