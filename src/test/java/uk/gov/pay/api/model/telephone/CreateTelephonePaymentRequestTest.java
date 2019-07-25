package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;

import static io.dropwizard.testing.FixtureHelpers.fixture;


public class CreateTelephonePaymentRequestTest {
    
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    
    @Test
    public void correctlyDeserializesFromJSON() throws Exception {
        final Supplemental supplemental = new Supplemental("ECKOH01234", "textual message describing error code");
        final PaymentOutcome paymentOutcome = new PaymentOutcome("success", "P0010", supplemental);
        final CreateTelephonePaymentRequest createTelephonePaymentRequest = new CreateTelephonePaymentRequest(
                12000, 
                "MRPC12345",
                "New passport application",
                "2018-02-21T16:04:25Z",
                "2018-02-21T16:05:33Z",
                "183f2j8923j8",
                "17498-8412u9-1273891239",
                "666",
                paymentOutcome,
                "master-card",
                "Jane Doe",
                "jane_doe@example.com",
                "02/19",
                "1234",
                "654321",
                "+447700900796");
        
        CreateTelephonePaymentRequest deserializedCreateTelephonePaymentRequest = MAPPER.readValue(fixture("fixtures/CreateTelephonePayment.json"), CreateTelephonePaymentRequest.class);
        
        assertThat(createTelephonePaymentRequest, samePropertyValuesAs(deserializedCreateTelephonePaymentRequest));
    }
}
