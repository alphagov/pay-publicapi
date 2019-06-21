package uk.gov.pay.api.utils.mocks;

import uk.gov.pay.api.model.directdebit.mandates.MandateState;

public class CreateMandateRequestParams {
    private final String mandateId;
    private final String serviceReference;
    private final String returnUrl;
    private final String createdDate;
    private final MandateState state;
    private final String gatewayAccountId;
    private final String chargeTokenId;
    private final String description;
    
    private CreateMandateRequestParams(String mandateId, String serviceReference, String returnUrl, 
                                       String createdDate, MandateState state, String gatewayAccountId, 
                                       String chargeTokenId, String description) {
        this.mandateId = mandateId;
        this.serviceReference = serviceReference;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
        this.gatewayAccountId = gatewayAccountId;
        this.chargeTokenId = chargeTokenId;
        this.description = description;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public MandateState getState() {
        return state;
    }

    public String getGatewayAccountId() {
        return gatewayAccountId;
    }

    public String getChargeTokenId() {
        return chargeTokenId;
    }

    public String getDescription() {
        return description;
    }

    public static final class CreateMandateRequestParamsBuilder {
        private String mandateId;
        private String serviceReference;
        private String returnUrl;
        private String createdDate;
        private MandateState state;
        private String gatewayAccountId;
        private String chargeTokenId;
        private String description;

        private CreateMandateRequestParamsBuilder() {
        }

        public static CreateMandateRequestParamsBuilder aCreateMandateRequestParams() {
            return new CreateMandateRequestParamsBuilder();
        }

        public CreateMandateRequestParamsBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public CreateMandateRequestParamsBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public CreateMandateRequestParamsBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public CreateMandateRequestParamsBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CreateMandateRequestParamsBuilder withState(MandateState state) {
            this.state = state;
            return this;
        }

        public CreateMandateRequestParamsBuilder withGatewayAccountId(String gatewayAccountId) {
            this.gatewayAccountId = gatewayAccountId;
            return this;
        }

        public CreateMandateRequestParamsBuilder withChargeTokenId(String chargeTokenId) {
            this.chargeTokenId = chargeTokenId;
            return this;
        }

        public CreateMandateRequestParamsBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CreateMandateRequestParams build() {
            return new CreateMandateRequestParams(mandateId, serviceReference, returnUrl, createdDate, state, gatewayAccountId, chargeTokenId, description);
        }
    }
}
