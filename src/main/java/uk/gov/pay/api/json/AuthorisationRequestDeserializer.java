package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.pay.api.exception.BadAuthorisationRequestException;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.AuthorisationRequest;

import java.io.IOException;

import static uk.gov.pay.api.json.RequestJsonParser.parseAuthorisationRequest;
import static uk.gov.pay.api.model.RequestError.Code.AUTHORISATION_API_PARSING_ERROR;
import static uk.gov.pay.api.model.RequestError.aRequestError;


public class AuthorisationRequestDeserializer extends StdDeserializer<AuthorisationRequest> {

    public AuthorisationRequestDeserializer() {
        super(AuthorisationRequest.class);
    }

    @Override
    public AuthorisationRequest deserialize(JsonParser parser, DeserializationContext context) {
        try {
            JsonNode json = parser.readValueAsTree();
            return parseAuthorisationRequest(json);
        } catch (IOException e) {
            throw new BadAuthorisationRequestException(aRequestError(AUTHORISATION_API_PARSING_ERROR));
        } catch (BadRequestException ex) {
            throw new BadAuthorisationRequestException(ex.getRequestError());
        }
    }
}
