package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.StringJoiner;

import static uk.gov.pay.api.model.CreateCardPaymentRequest.RETURN_URL_FIELD_NAME;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreateDirectDebitPaymentRequest extends CreatePaymentRequest{

    public static final int AGREEMENT_ID_MAX_LENGTH = 26;
    public static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";

    @Size(max = AGREEMENT_ID_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = AGREEMENT_ID_FIELD_NAME)
    private final String agreementId;


    public CreateDirectDebitPaymentRequest(CreatePaymentRequestBuilder createPaymentRequestBuilder) {
        super(createPaymentRequestBuilder);
        this.agreementId = createPaymentRequestBuilder.getAgreementId();
    }

    @Override
    @Min(value = 1, message = "Must be greater than or equal to 1")
    public int getAmount() {
        return super.getAmount();
    }

    @ApiModelProperty(value = "ID of the agreement being used to collect the payment", required = false, example = "33890b55-b9ea-4e2f-90fd-77ae0e9009e2")
    public String getAgreementId() {
        return agreementId;
    }

    @Override
    public String toConnectorPayload() {
        JsonStringBuilder request = new JsonStringBuilder()
                .add("amount", this.getAmount())
                .add("reference", this.getReference())
                .add("description", this.getDescription())
                .add("agreement_id", agreementId);
        getLanguage().ifPresent(language -> request.add("language", language.toString()));
        getEmail().ifPresent(email -> request.add("email", email));

        return request.build();
    }

    @Override
    public void validateRequestType(Account account) {
        if (account.getPaymentType() != TokenPaymentType.DIRECT_DEBIT) {
            throw new BadRequestException(PaymentError.aPaymentError(RETURN_URL_FIELD_NAME, PaymentError.Code.CREATE_PAYMENT_MISSING_FIELD_ERROR));
        }
    }

    /**
     * This looks JSONesque but is not identical to the received request
     */
    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("amount: ").add(String.valueOf(super.getAmount()));
        joiner.add("reference: ").add(super.getReference());
        joiner.add("agreement_id: ").add(agreementId);
        super.getLanguage().ifPresent(value -> joiner.add("language: ").add(value.toString()));
        return joiner.toString();
    }
}
