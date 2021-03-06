package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(ALWAYS)
@ApiModel(value = "CardDetails", description = "A structure representing the payment card")
@Schema(name = "CardDetails", description = "A structure representing the payment card")
@JsonIgnoreProperties(ignoreUnknown = true)
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
    
    @JsonProperty("card_type")
    private final String cardType;

    public CardDetails(@JsonProperty("last_digits_card_number") String lastDigitsCardNumber,
                       @JsonProperty("first_digits_card_number") String firstDigitsCardNumber,
                       @JsonProperty("cardholder_name") String cardHolderName,
                       @JsonProperty("expiry_date") String expiryDate,
                       @JsonProperty("billing_address") Address billingAddress,
                       @JsonProperty("card_brand") String cardBrand,
                       @JsonProperty("card_type") String cardType) {
        this.lastDigitsCardNumber = lastDigitsCardNumber;
        this.firstDigitsCardNumber = firstDigitsCardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.billingAddress = billingAddress;
        this.cardBrand = cardBrand;
        this.cardType = cardType;
    }

    @ApiModelProperty(example = "1234")
    @Schema(example = "1234", accessMode = READ_ONLY)
    public String getLastDigitsCardNumber() {
        return lastDigitsCardNumber;
    }

    @ApiModelProperty(example = "123456")
    @Schema(example = "123456", accessMode = READ_ONLY)
    public String getFirstDigitsCardNumber() {
        return firstDigitsCardNumber;
    }

    @ApiModelProperty(example = "Mr. Card holder")
    @Schema(example = "Mr. Card holder")
    public String getCardHolderName() {
        return cardHolderName;
    }

    @ApiModelProperty(value = "The expiry date of the card in MM/yy format", example = "04/24")
    @Schema(description = "The expiry date of the card in MM/yy format", example = "04/24", accessMode = READ_ONLY)
    public String getExpiryDate() {
        return expiryDate;
    }

    public Optional<Address> getBillingAddress() {
        return Optional.ofNullable(billingAddress);
    }

    @ApiModelProperty(example = "Visa")
    @Schema(example = "Visa", accessMode = READ_ONLY)
    public String getCardBrand() {
        return cardBrand;
    }

    @ApiModelProperty(value = "The card type, `debit` or `credit` or `null` if not able to determine", allowableValues = "debit,credit,null", example = "debit")
    @Schema(description = "The card type, `debit` or `credit` or `null` if not able to determine", allowableValues = {"debit","credit","null"}, example = "debit", accessMode = READ_ONLY)
    public String getCardType() {
        return cardType;
    }
}
