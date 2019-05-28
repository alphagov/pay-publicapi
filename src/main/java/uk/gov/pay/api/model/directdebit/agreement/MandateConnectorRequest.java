package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateConnectorRequest {

    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String SERVICE_REFERENCE_FIELD_NAME = "service_reference";

    private String returnUrl;
    private String serviceReference;

    public MandateConnectorRequest(String returnUrl, String serviceReference) {
        this.returnUrl = returnUrl;
        this.serviceReference = serviceReference;
    }

    public static MandateConnectorRequest from(CreateAgreementRequest request) {
        return new MandateConnectorRequest(request.getReturnUrl(), request.getReference());
    }

    @JsonProperty(value = RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
    }

    @JsonProperty(value = SERVICE_REFERENCE_FIELD_NAME)
    public String getServiceReference() {
        return serviceReference;
    }
}
