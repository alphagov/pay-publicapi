package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.HttpMethod.GET;

public abstract class LinksResponse {
    private List<JsonLink> links = new ArrayList<>();

    public LinksResponse addSelfLink(URI href) {
        links.add(new JsonLink("self", GET, href.toString()));
        return this;
    }

    public LinksResponse addLink(String rel, String method, String href) {
        links.add(new JsonLink(rel, method, href));
        return this;
    }

    public List<JsonLink> getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "links=[" + Joiner.on(", ").join(links) + ']';
    }

    @JsonSnakeCase
    class JsonLink {
        private final String rel;
        private final String method;
        private final String href;

        private JsonLink(String rel, String method, String href) {
            this.rel = rel;
            this.method = method;
            this.href = href;
        }

        @ApiModelProperty(example = "self")
        @NotBlank
        public String getRel() {
            return rel;
        }

        @ApiModelProperty(example = "GET")
        @NotBlank
        public String getMethod() {
            return method;
        }

        @ApiModelProperty(example = "http://payments.gov.uk/v1/payments/1122335")
        @NotBlank
        public String getHref() {
            return href;
        }

        @Override
        public String toString() {
            return "{ rel='" + rel + '\'' +
                    ", method='" + method + '\'' +
                    ", href='" + href + "' }";
        }
    }
}
