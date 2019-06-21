package uk.gov.pay.api.utils.mocks;

import uk.gov.pay.api.model.directdebit.mandates.MandateState;

public class DDConnectorResponseToGetMandateParams {

    private final String mandateId;
    private final String mandateReference;
    private final String serviceReference;
    private final String returnUrl;
    private final MandateState state;
    private final String gatewayAccountId;
    private final String chargeTokenId;

    private DDConnectorResponseToGetMandateParams(String mandateId, 
                                                  String mandateReference, 
                                                  String serviceReference, 
                                                  String returnUrl, 
                                                  MandateState state, 
                                                  String gatewayAccountId, 
                                                  String chargeTokenId) {
        this.mandateId = mandateId;
        this.mandateReference = mandateReference;
        this.serviceReference = serviceReference;
        this.returnUrl = returnUrl;
        this.state = state;
        this.gatewayAccountId = gatewayAccountId;
        this.chargeTokenId = chargeTokenId;
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

    public static final class DDConnectorResponseToGetMandateParamsBuilder {
        private String mandateId;
        private String mandateReference;
        private String serviceReference;
        private String returnUrl;
        private MandateState state;
        private String gatewayAccountId;
        private String chargeTokenId;

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

        public DDConnectorResponseToGetMandateParams build() {
            return new DDConnectorResponseToGetMandateParams(mandateId, mandateReference, serviceReference, returnUrl, state, gatewayAccountId, chargeTokenId);
        }
    }
}
