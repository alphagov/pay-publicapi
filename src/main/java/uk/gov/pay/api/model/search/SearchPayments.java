package uk.gov.pay.api.model.search;

import black.door.hate.HalRepresentation;
import uk.gov.pay.api.auth.Account;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

public interface SearchPayments {
    Response getSearchResponse(Account account, Map<String, String> queryParams);
}
