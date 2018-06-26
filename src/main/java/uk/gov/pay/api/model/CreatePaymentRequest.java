package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreatePaymentRequest {

    public static final String AMOUNT_FIELD_NAME="amount";
    public static final String RETURN_URL_FIELD_NAME="return_url";
    public static final String REFERENCE_FIELD_NAME="reference";
    public static final String DESCRIPTION_FIELD_NAME="description";
    public static final String AGREEMENT_ID_FIELD_NAME="agreement_id";

    private int amount;
    private String returnUrl;
    private String reference;
    private String description;
    private String agreementId = null;

    public CreatePaymentRequest(int amount, String returnUrl, String reference, String description) {
        this(amount, returnUrl, reference, description, null);
    }

    public CreatePaymentRequest(int amount, String returnUrl, String reference, String description, String agreementId) {
        this.amount = amount;
        this.returnUrl = returnUrl;
        this.reference = reference;
        this.description = description;
        this.agreementId = agreementId;    
    }

    @ApiModelProperty(value = "service return url", required = false, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    public int getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "payment reference", required = true, example = "12345")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "payment description", required = true, example = "New passport application")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "CreatePaymentRequest{" +
                "amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", agreement_id='" + agreementId + '\'' +
                '}';
    }

    public boolean hasReturnUrl() {
        return StringUtils.isNotBlank(returnUrl);
    }

    @ApiModelProperty(value = "ID of the agreement being used to collect the payment", required = false, example = "33890b55-b9ea-4e2f-90fd-77ae0e9009e2\n")
    @JsonProperty(AGREEMENT_ID_FIELD_NAME)
    public String getAgreementId() {
        return agreementId;
    }

    public boolean hasAgreementId() {
        return StringUtils.isNotBlank(agreementId);
    }
}
