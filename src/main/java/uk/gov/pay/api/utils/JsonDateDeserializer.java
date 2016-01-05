package uk.gov.pay.api.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class JsonDateDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        ObjectCodec oc = jp.getCodec();
        TextNode node =  oc.readTree(jp);
        String dateString = node.textValue();

        Instant instant = Instant.parse(dateString);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime date = LocalDateTime.of(
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond());
        return date;
    }
}
