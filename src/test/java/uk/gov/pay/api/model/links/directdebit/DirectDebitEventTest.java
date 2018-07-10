package uk.gov.pay.api.model.links.directdebit;

import org.junit.Test;

import static org.junit.Assert.*;

public class DirectDebitEventTest {
    
    @Test
    public void testAgreementLink() {
        DirectDebitEvent ddEvent = DirectDebitEvent.builder().mandateExternalId("1").build();
        assertEquals("/v1/agreements/1", ddEvent.getLinks().getAgreement());
        assertNull(ddEvent.getLinks().getPayment());
    }
    
    @Test
    public void testPaymentLink() {
        DirectDebitEvent ddEvent = DirectDebitEvent.builder().transactionExternalId("2").build();
        assertEquals("/v1/payments/2", ddEvent.getLinks().getPayment());
        assertNull(ddEvent.getLinks().getAgreement());
    }

}
