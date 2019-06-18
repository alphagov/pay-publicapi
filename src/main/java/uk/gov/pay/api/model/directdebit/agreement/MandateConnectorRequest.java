package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MandateConnectorRequest {

    @JsonProperty("return_url")
    private final String returnUrl;
    
    @JsonProperty("service_reference")
    private final String serviceReference;
    
    private final String description;

    public MandateConnectorRequest(String returnUrl, String serviceReference, String description) {
        this.returnUrl = returnUrl;
        this.serviceReference = serviceReference;
        this.description = description;
    }

    public static MandateConnectorRequest from(CreateMandateRequest request) {
        return new MandateConnectorRequest(request.getReturnUrl(), request.getReference(), request.getDescription());
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public String getDescription() {
        return description;
    }
}
