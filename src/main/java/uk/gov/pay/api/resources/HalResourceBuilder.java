package uk.gov.pay.api.resources;

import black.door.hate.HalRepresentation;
import black.door.hate.HalRepresentation.HalRepresentationBuilder;
import black.door.hate.HalResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.mapper.SearchChargesExceptionMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HalResourceBuilder implements HalResource {
    public static final String SELF_LINK_KEY = "self";
    private Map<String, URI> linkMap = new HashMap<>();
    private Map<String, Object> propertyMap = new HashMap<>();
    private URI selfLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(HalResourceBuilder.class);

    public HalResourceBuilder(URI selfLink) {
        try {
            this.selfLink = selfLink != null ? selfLink: new URI("");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public HalResourceBuilder withLink(String linkKey, URI link) {
        if (linkKey!=null && link !=null) {
            linkMap.put(linkKey, link);
        }
        return this;
    }

    public HalResourceBuilder withProperty(String key, Object value) {
        if (key!=null && value!=null) {
            propertyMap.put(key, value);
        }
        return this;
    }

    @Override
    public URI location() {
        return selfLink;
    }

    @Override
    public HalRepresentationBuilder representationBuilder() {
        HalRepresentationBuilder builder = HalRepresentation.builder();
        builder.addLink(SELF_LINK_KEY, this);

        for (String key : linkMap.keySet()) {
            builder.addLink(key, linkMap.get(key));
        }
        for (String key : propertyMap.keySet()) {
            builder.addProperty(key, propertyMap.get(key));
        }
        return builder;
    }

    public String build() {
        try {
            return representationBuilder().build().serialize();
        } catch (JsonProcessingException e) {
            LOGGER.error("error occurred while building the HAL resource with navigation links", e);
            throw new RuntimeException("error occurred while building the HAL resource with navigation links", e);
        }
    }
}
