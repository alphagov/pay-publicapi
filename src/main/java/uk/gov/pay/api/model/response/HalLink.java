package uk.gov.pay.api.model.response;

import java.util.Map;
import java.util.Objects;

public class HalLink {

    private String rel;
    private String href;
    private String method;
    private String type;
    private Map<String, Object> params;

    // required for Jackson
    private HalLink() {
    }

    public HalLink(String rel,
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
        HalLink that = (HalLink) o;
        return Objects.equals(rel, that.rel) &&
                Objects.equals(href, that.href) &&
                Objects.equals(method, that.method) &&
                Objects.equals(type, that.type) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rel, href, method, type, params);
    }
}
