package uk.gov.pay.api.utils.mocks;

import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.utils.ChargeEventBuilder;

public class DDConnectorResponseToGetMandateParams {

    private final String mandateId;
    private final String mandateReference;
    private final String serviceReference;
    private final String returnUrl;
    private final MandateState state;
    private final String gatewayAccountId;
    private final String chargeTokenId;
    private final String providerId;
    private final String createdDate;

    private DDConnectorResponseToGetMandateParams(String mandateId,
                                                  String mandateReference,
                                                  String serviceReference,
                                                  String returnUrl,
                                                  MandateState state,
                                                  String gatewayAccountId,
                                                  String chargeTokenId,
                                                  String providerId, 
                                                  String createdDate) {
        this.mandateId = mandateId;
        this.mandateReference = mandateReference;
        this.serviceReference = serviceReference;
        this.returnUrl = returnUrl;
        this.state = state;
        this.gatewayAccountId = gatewayAccountId;
        this.chargeTokenId = chargeTokenId;
        this.providerId = providerId;
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getMandateReference() {
        return mandateReference;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public String getReturnUrl() {
        return returnUrl;
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

    public String getProviderId() {
        return providerId;
    }

    public static final class DDConnectorResponseToGetMandateParamsBuilder {
        private String mandateId;
        private String mandateReference;
        private String serviceReference;
        private String returnUrl;
        private MandateState state;
        private String gatewayAccountId;
        private String chargeTokenId;
        private String providerId;
        private String createdDate;

        private DDConnectorResponseToGetMandateParamsBuilder() {
        }

        public static DDConnectorResponseToGetMandateParamsBuilder aDDConnectorResponseToGetMandateParams() {
            return new DDConnectorResponseToGetMandateParamsBuilder();
        }

        public DDConnectorResponseToGetMandateParamsBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withMandateReference(String mandateReference) {
            this.mandateReference = mandateReference;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withState(MandateState state) {
            this.state = state;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withGatewayAccountId(String gatewayAccountId) {
            this.gatewayAccountId = gatewayAccountId;
            return this;
        }

        public DDConnectorResponseToGetMandateParamsBuilder withChargeTokenId(String chargeTokenId) {
            this.chargeTokenId = chargeTokenId;
            return this;
        }
        
        public DDConnectorResponseToGetMandateParamsBuilder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public DDConnectorResponseToGetMandateParams build() {
            return new DDConnectorResponseToGetMandateParams(mandateId, mandateReference, serviceReference, returnUrl, 
                    state, gatewayAccountId, chargeTokenId, providerId, createdDate);
        }

        public DDConnectorResponseToGetMandateParamsBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }
    }
}
