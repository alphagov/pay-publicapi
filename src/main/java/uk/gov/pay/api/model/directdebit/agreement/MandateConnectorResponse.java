package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateConnectorResponse {

    private String mandateId;
    private MandateType mandateType;
    private String mandateReference;
    private String serviceReference;
    private String returnUrl;
    private String createdDate;
    private MandateState state;
    private List<PaymentConnectorResponseLink> links = new ArrayList<>();

    @JsonProperty(value = "mandate_id")
    public String getMandateId() {
        return mandateId;
    }

    @JsonProperty(value = "mandate_type")
    public MandateType getMandateType() {
        return mandateType;
    }

    @JsonProperty(value = "mandate_reference")
    public String getMandateReference() { return mandateReference; }

    @JsonProperty(value = "service_reference")
    public String getServiceReference() {
        return serviceReference;
    }

    @JsonProperty(value = "return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @JsonProperty(value = "created_date")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty(value = "state")
    public MandateState getState() {
        return state;
    }

    @JsonProperty(value = "links")
    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }

}
