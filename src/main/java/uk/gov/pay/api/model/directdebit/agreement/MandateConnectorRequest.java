package uk.gov.pay.api.model.directdebit.agreement;

public class MandateConnectorRequest {

    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String SERVICE_REFERENCE_FIELD_NAME = "service_reference";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    private final String returnUrl, serviceReference, description;

    public MandateConnectorRequest(String returnUrl, String serviceReference, String description) {
        this.returnUrl = returnUrl;
        this.serviceReference = serviceReference;
        this.description = description;
    }

    public static MandateConnectorRequest from(CreateAgreementRequest request) {
        return new MandateConnectorRequest(request.getReturnUrl(), request.getReference(), "raindrops on roses and whiskers on kittens");
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
