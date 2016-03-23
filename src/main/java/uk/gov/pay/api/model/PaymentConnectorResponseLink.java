package uk.gov.pay.api.model;

import java.util.Map;

public class PaymentConnectorResponseLink {

    private String rel;
    private String href;
    private String method;
    private String type;
    private Map<String, Object> params;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getMethod() {
        return method;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
