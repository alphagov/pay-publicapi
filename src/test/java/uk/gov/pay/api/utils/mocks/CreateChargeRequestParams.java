package uk.gov.pay.api.utils.mocks;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateChargeRequestParams {
    
    private final int amount;
    private final String returnUrl, description, reference;
    private final Map<String, Object> metadata;
    private final String email;

    private CreateChargeRequestParams(int amount, 
                                      String returnUrl, 
                                      String description, 
                                      String reference, 
                                      Map<String, Object> metadata,
                                      String email) {
        this.amount = amount;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.metadata = metadata;
        this.email = email;
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

    public static final class CreateChargeRequestParamsBuilder {
        private Integer amount;
        private String returnUrl;
        private String description;
        private String reference;
        private Map<String, Object> metadata = Map.of();
        private String email;

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

        public CreateChargeRequestParams build() {
            List.of(amount, reference, returnUrl, description).forEach(Objects::requireNonNull);
            return new CreateChargeRequestParams(amount, returnUrl, description, reference, metadata, email);
        }
    }
}
