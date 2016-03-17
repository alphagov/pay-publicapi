package uk.gov.pay.api.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Map;

public class ChargeEventBuilder {

    @JsonDeserialize
    @JsonSerialize
    @JsonProperty("charge_id")
    private String chargeId;

    @JsonDeserialize
    @JsonSerialize
    private String status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    public ChargeEventBuilder(String chargeId, String status, LocalDateTime updated) {
        this.chargeId = chargeId;
        this.status = status;
        this.updated = updated;
    }

    public Map build() {
        ObjectMapper mapper = new ObjectMapper();
        Map node = mapper.convertValue(this, Map.class);
        return node;
    }

}
