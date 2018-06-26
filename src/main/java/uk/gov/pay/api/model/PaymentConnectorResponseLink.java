package uk.gov.pay.api.model;

import java.util.Map;

public class PaymentConnectorResponseLink {

    private String rel;
    private String href;
    private String method;
    private String type;
    private Map<String, Object> params;

    // required for Jackson
    private PaymentConnectorResponseLink() {
    }

    public PaymentConnectorResponseLink(String rel,
                                        String href,
                                        String method,
                                        String type,
                                        Map<String, Object> params) {
        this.rel = rel;
        this.href = href;
        this.method = method;
        this.type = type;
        this.params = params;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentConnectorResponseLink that = (PaymentConnectorResponseLink) o;

        if (!rel.equals(that.rel)) return false;
        if (!href.equals(that.href)) return false;
        if (!method.equals(that.method)) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return params != null ? params.equals(that.params) : that.params == null;
    }

    @Override
    public int hashCode() {
        int result = rel.hashCode();
        result = 31 * result + href.hashCode();
        result = 31 * result + method.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
