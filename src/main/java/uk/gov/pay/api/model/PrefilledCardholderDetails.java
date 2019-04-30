package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrefilledCardholderDetails {

    @JsonProperty("cardholder_name")
    private String cardholderName;
    
    @JsonProperty("billing_address")
    private Address billingAddress;

    public Optional<Address> getBillingAddress() {
        return Optional.ofNullable(billingAddress);
    }
    
    public Optional<String> getCardholderName() {
        return Optional.ofNullable(cardholderName);
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }
    
    public void setAddress(String addressLine1, String addressLine2, String postcode, String city, String country) {
        this.billingAddress = new Address(addressLine1, addressLine2, postcode, city, country);
    }
}
