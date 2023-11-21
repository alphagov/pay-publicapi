package uk.gov.pay.api.model.tink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/*
for e.g.
{
  "token_type" : "bearer",
  "expires_in" : 1799,
  "access_token" : "{YOUR_CLIENT_ACCESS_TOKEN}",
  "scope" : "payment:read,payment:write",
  "id_hint" : null
}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TinkTokenResponse {
    
    private String tokenType;
    
    private String accessToken;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
