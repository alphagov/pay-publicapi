package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@JsonInclude(ALWAYS)
@ApiModel(value = "CardDetails", description = "A structure representing the payment card")
public class CardDetails {

    @JsonProperty("last_digits_card_number")
    private final String lastDigitsCardNumber;

    @JsonProperty("first_digits_card_number")
    private final String firstDigitsCardNumber;

    @JsonProperty("cardholder_name")
    private final String cardHolderName;

    @JsonProperty("expiry_date")
    private final String expiryDate;

    @JsonProperty("billing_address")
    private final Address billingAddress;

    @JsonProperty("card_brand")
    private final String cardBrand;

    public CardDetails(@JsonProperty("last_digits_card_number") String lastDigitsCardNumber,
                       @JsonProperty("first_digits_card_number") String firstDigitsCardNumber,
                       @JsonProperty("cardholder_name") String cardHolderName,
                       @JsonProperty("expiry_date") String expiryDate,
                       @JsonProperty("billing_address") Address billingAddress,
                       @JsonProperty("card_brand") String cardBrand) {
        this.lastDigitsCardNumber = lastDigitsCardNumber;
        this.firstDigitsCardNumber = firstDigitsCardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.billingAddress = billingAddress;
        this.cardBrand = cardBrand;
    }

    @ApiModelProperty(example = "1234")
    public String getLastDigitsCardNumber() {
        return lastDigitsCardNumber;
    }

    @ApiModelProperty(example = "123456")
    public String getFirstDigitsCardNumber() {
        return firstDigitsCardNumber;
    }

    @ApiModelProperty(example = "Mr. Card holder")
    public String getCardHolderName() {
        return cardHolderName;
    }

    @ApiModelProperty(example = "12/20")
    public String getExpiryDate() {
        return expiryDate;
    }

    public Optional<Address> getBillingAddress() {
        return Optional.ofNullable(billingAddress);
    }

    @ApiModelProperty(example = "Visa")
    public String getCardBrand() {
        return cardBrand;
    }
}
