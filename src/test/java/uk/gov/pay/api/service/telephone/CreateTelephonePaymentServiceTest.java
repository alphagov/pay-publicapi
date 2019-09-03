package uk.gov.pay.api.service.telephone;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;
import uk.gov.pay.api.model.telephone.Supplemental;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;

public class CreateTelephonePaymentServiceTest {
    
    /*
    private CreateTelephonePaymentService createTelephonePaymentService = new CreateTelephonePaymentService();
    
    @Test
    public void createsTelephonePaymentResponseWhenStatusIsFailed() {
        Supplemental supplemental = new Supplemental(
                "ECKOH01234",
                "textual message describing error code"
        );
        PaymentOutcome paymentOutcome = new PaymentOutcome(
                "failed",
                "P0010",
                supplemental
        );
        CreateTelephonePaymentRequest createTelephonePaymentRequest = new CreateTelephonePaymentRequest(
                12000,
                "Some reference",
                "Some description",
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
                "123456",
                "+447700900796"
        );
        TelephonePaymentResponse telephonePaymentResponse = createTelephonePaymentService.create(createTelephonePaymentRequest);
        assertThat(telephonePaymentResponse.getAmount(), is(12000));
        assertThat(telephonePaymentResponse.getReference(), is("Some reference"));
        assertThat(telephonePaymentResponse.getDescription(), is("Some description"));
        assertThat(telephonePaymentResponse.getCreatedDate(), is("2018-02-21T16:04:25Z"));
        assertThat(telephonePaymentResponse.getAuthorisedDate(), is("2018-02-21T16:05:33Z"));
        assertThat(telephonePaymentResponse.getProcessorId(), is("183f2j8923j8"));
        assertThat(telephonePaymentResponse.getProviderId(), is("17498-8412u9-1273891239"));
        assertThat(telephonePaymentResponse.getAuthCode(), is("666"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getStatus(), is("failed"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getCode(), is("P0010"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getSupplemental().get().getErrorCode(), is("ECKOH01234"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getSupplemental().get().getErrorMessage(), is("textual message describing error code"));
        assertThat(telephonePaymentResponse.getCardType(), is("master-card"));
        assertThat(telephonePaymentResponse.getNameOnCard(), is("Jane Doe"));
        assertThat(telephonePaymentResponse.getEmailAddress(), is("jane_doe@example.com"));
        assertThat(telephonePaymentResponse.getCardExpiry(), is("02/19"));
        assertThat(telephonePaymentResponse.getLastFourDigits(), is("1234"));
        assertThat(telephonePaymentResponse.getFirstSixDigits(), is("123456"));
        assertThat(telephonePaymentResponse.getTelephoneNumber(), is("+447700900796"));
    }
    
    @Test
    public void createsTelephonePaymentResponseWhenStatusIsSuccessAndSupplementalMissingFromJSON() {
        PaymentOutcome paymentOutcome = new PaymentOutcome(
                "success",
                null,
                null
        );

        CreateTelephonePaymentRequest createTelephonePaymentRequest = new CreateTelephonePaymentRequest(
                12000,
                "Some reference",
                "Some description",
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
                "123456",
                "+447700900796"
        );
        TelephonePaymentResponse telephonePaymentResponse = createTelephonePaymentService.create(createTelephonePaymentRequest);

        assertThat(telephonePaymentResponse.getAmount(), is(12000));
        assertThat(telephonePaymentResponse.getReference(), is("Some reference"));
        assertThat(telephonePaymentResponse.getDescription(), is("Some description"));
        assertThat(telephonePaymentResponse.getCreatedDate(), is("2018-02-21T16:04:25Z"));
        assertThat(telephonePaymentResponse.getAuthorisedDate(), is("2018-02-21T16:05:33Z"));
        assertThat(telephonePaymentResponse.getProcessorId(), is("183f2j8923j8"));
        assertThat(telephonePaymentResponse.getProviderId(), is("17498-8412u9-1273891239"));
        assertThat(telephonePaymentResponse.getAuthCode(), is("666"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getStatus(), is("success"));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getCode(), is(nullValue()));
        assertThat(telephonePaymentResponse.getPaymentOutcome().getSupplemental().isPresent(), is(false));
        assertThat(telephonePaymentResponse.getCardType(), is("master-card"));
        assertThat(telephonePaymentResponse.getNameOnCard(), is("Jane Doe"));
        assertThat(telephonePaymentResponse.getEmailAddress(), is("jane_doe@example.com"));
        assertThat(telephonePaymentResponse.getCardExpiry(), is("02/19"));
        assertThat(telephonePaymentResponse.getLastFourDigits(), is("1234"));
        assertThat(telephonePaymentResponse.getFirstSixDigits(), is("123456"));
        assertThat(telephonePaymentResponse.getTelephoneNumber(), is("+447700900796"));
        
    }
    */
}
