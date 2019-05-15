package uk.gov.pay.api.utils.mocks;

import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CreateChargeRequestParams {
    
    private final int amount;
    private final String returnUrl, description, reference;
    private final Map<String, Object> metadata;
    private final String email;
    private final String cardholderName;
    private final String addressLine1;
    private final String addressLine2;
    private final String addressPostcode;
    private final String addressCity;
    private final String addressCountry;
    private final SupportedLanguage language;

    private CreateChargeRequestParams(CreateChargeRequestParamsBuilder builder) {
        this.amount = builder.amount;
        this.returnUrl = builder.returnUrl;
        this.description = builder.description;
        this.reference = builder.reference;
        this.metadata = builder.metadata;
        this.email = builder.email;
        this.cardholderName = builder.cardholderName;
        this.addressLine1 = builder.addressLine1;
        this.addressLine2 = builder.addressLine2;
        this.addressPostcode = builder.addressPostcode;
        this.addressCity = builder.addressCity;
        this.addressCountry = builder.addressCountry;
        this.language = builder.language;
    }

    public int getAmount() {
        return amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getEmail() {
        return email;
    }

    public Optional<String> getCardholderName() {
        return Optional.ofNullable(cardholderName);
    }

    public Optional<String> getAddressLine1() {
        return Optional.ofNullable(addressLine1);
    }

    public Optional<String> getAddressLine2() {
        return Optional.ofNullable(addressLine2);
    }

    public Optional<String> getAddressPostcode() {
        return Optional.ofNullable(addressPostcode);
    }

    public Optional<String> getAddressCity() {
        return Optional.ofNullable(addressCity);
    }

    public Optional<String> getAddressCountry() {
        return Optional.ofNullable(addressCountry);
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public static final class CreateChargeRequestParamsBuilder {
        private Integer amount;
        private String returnUrl;
        private String description;
        private String reference;
        private Map<String, Object> metadata = Map.of();
        private String email;
        private String cardholderName;
        private String addressLine1;
        private String addressLine2;
        private String addressPostcode;
        private String addressCity;
        private String addressCountry;
        private SupportedLanguage language;

        private CreateChargeRequestParamsBuilder() {
        }

        public static CreateChargeRequestParamsBuilder aCreateChargeRequestParams() {
            return new CreateChargeRequestParamsBuilder();
        }

        public CreateChargeRequestParamsBuilder withAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public CreateChargeRequestParamsBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public CreateChargeRequestParamsBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CreateChargeRequestParamsBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public CreateChargeRequestParamsBuilder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public CreateChargeRequestParamsBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public CreateChargeRequestParamsBuilder witCardHolderName(String cardholderName) {
            this.cardholderName = cardholderName;
            return this;
        }
        
        public CreateChargeRequestParamsBuilder withAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
            return this;
        }

        public CreateChargeRequestParamsBuilder withAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public CreateChargeRequestParamsBuilder withAddressPostcode(String addressPostcode) {
            this.addressPostcode = addressPostcode;
            return this;
        }

        public CreateChargeRequestParamsBuilder withAddressCity(String addressCity) {
            this.addressCity = addressCity;
            return this;
        }

        public CreateChargeRequestParamsBuilder withAddressCountry(String addressCountry) {
            this.addressCountry = addressCountry;
            return this;
        }

        public CreateChargeRequestParams build() {
            List.of(amount, reference, returnUrl, description).forEach(Objects::requireNonNull);
            return new CreateChargeRequestParams(this);
        }

        public CreateChargeRequestParamsBuilder withLanguage(SupportedLanguage language) {
            this.language = language;
            return this;
        }
    }
}
