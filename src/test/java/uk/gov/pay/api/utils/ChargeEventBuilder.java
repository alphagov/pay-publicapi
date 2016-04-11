package uk.gov.pay.api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public class ChargeEventBuilder {

    @JsonDeserialize
    @JsonSerialize
    @JsonProperty("charge_id")
    private String chargeId;

    @JsonDeserialize
    @JsonSerialize
    private String status;

    @JsonDeserialize
    @JsonSerialize
    private String updated;

    public ChargeEventBuilder(String chargeId, String status, String updated) {
        this.chargeId = chargeId;
        this.status = status;
        this.updated = updated;
    }

    public Map build() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(this, Map.class);
    }

}
