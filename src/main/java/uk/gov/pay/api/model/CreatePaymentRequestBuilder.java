package uk.gov.pay.api.model;

import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

public class CreatePaymentRequestBuilder {
    private ExternalMetadata metadata;
    private int amount;
    private String returnUrl;
    private String reference;
    private String description;
    private String agreementId;
    private SupportedLanguage language;
    private Boolean delayedCapture;
    private String email;
    private String cardholderName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postcode;
    private String country;
    private PrefilledCardholderDetails prefilledCardholderDetails;

    public CreatePaymentRequest build() {
        return this.getMandateId() == null ? new CreateCardPaymentRequest(this) : new CreateDirectDebitPaymentRequest(this);
    }

    public static CreatePaymentRequestBuilder builder() {
        return new CreatePaymentRequestBuilder();
    }

    public CreatePaymentRequestBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public CreatePaymentRequestBuilder returnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }

    public CreatePaymentRequestBuilder reference(String reference) {
        this.reference = reference;
        return this;
    }

    public CreatePaymentRequestBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CreatePaymentRequestBuilder mandateId(String agreementId) {
        this.agreementId = agreementId;
        return this;
    }

    public CreatePaymentRequestBuilder language(SupportedLanguage language) {
        this.language = language;
        return this;
    }

    public CreatePaymentRequestBuilder delayedCapture(Boolean delayedCapture) {
        this.delayedCapture = delayedCapture;
        return this;
    }

    public CreatePaymentRequestBuilder metadata(ExternalMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreatePaymentRequestBuilder email(String email) {
        this.email = email;
        return this;
    }

    public CreatePaymentRequestBuilder cardholderName(String cardHolderName) {
        this.cardholderName = cardHolderName;
        return this;
    }

    public CreatePaymentRequestBuilder addressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public CreatePaymentRequestBuilder addressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public CreatePaymentRequestBuilder city(String city) {
        this.city = city;
        return this;
    }

    public CreatePaymentRequestBuilder postcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    public CreatePaymentRequestBuilder country(String country) {
        this.country = country;
        return this;
    }

    public PrefilledCardholderDetails getPrefilledCardholderDetails() {
        if (cardholderName != null) {
            this.prefilledCardholderDetails = new PrefilledCardholderDetails();
            this.prefilledCardholderDetails.setCardholderName(cardholderName);
        }
        if (addressLine1 != null || addressLine2 != null ||
                postcode != null || city != null || country != null) {
            if (this.prefilledCardholderDetails == null) {
                this.prefilledCardholderDetails = new PrefilledCardholderDetails();
            }
            this.prefilledCardholderDetails.setAddress(addressLine1, addressLine2, postcode, city, country);
        }
        return prefilledCardholderDetails;
    }

    public ExternalMetadata getMetadata() {
        return metadata;
    }

    public int getAmount() {
        return amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getMandateId() {
        return agreementId;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public Boolean getDelayedCapture() {
        return delayedCapture;
    }

    public String getEmail() {
        return email;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }
}
