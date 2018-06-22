package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateConnectorRequest {

    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String AGREEMENT_TYPE_FIELD_NAME = "agreement_type";
    public static final String SERVICE_REFERENCE_FIELD_NAME = "service_reference";

    private String returnUrl;
    private AgreementType agreementType;
    private String serviceReference;

    public MandateConnectorRequest(String returnUrl, AgreementType agreementType, String serviceReference) {
        this.returnUrl = returnUrl;
        this.agreementType = agreementType;
        this.serviceReference = serviceReference;
    }

    public static MandateConnectorRequest from(CreateAgreementRequest request) {
        return new MandateConnectorRequest(request.getReturnUrl(), request.getAgreementType(), request.getReference());
    }

    @JsonProperty(value = RETURN_URL_FIELD_NAME)
    public String getReturnUrl() {
        return returnUrl;
    }

    @JsonProperty(value = AGREEMENT_TYPE_FIELD_NAME)
    public AgreementType getAgreementType() {
        return agreementType;
    }

    @JsonProperty(value = SERVICE_REFERENCE_FIELD_NAME)
    public String getServiceReference() {
        return serviceReference;
    }
}
