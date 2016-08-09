package uk.gov.pay.api.model;

import black.door.hate.HalRepresentation;
import black.door.hate.HalResource;

import java.net.URI;

public abstract class HalResourceResponse implements HalResource {

    private final HalRepresentation.HalRepresentationBuilder halRepresentation;
    private final URI location;

    protected HalResourceResponse(HalRepresentation.HalRepresentationBuilder halRepresentation, URI location) {
        this.halRepresentation = halRepresentation;
        this.location = location;
        this.halRepresentation.addLink("self", this);
    }

    @Override
    public HalRepresentation.HalRepresentationBuilder representationBuilder() {
        return this.halRepresentation;
    }

    @Override
    public URI location() {
        return this.location;
    }

    public String serialize() {
        return this.asEmbedded().toString();
    }
}
