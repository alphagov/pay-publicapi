package uk.gov.pay.api.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by pmonteiro on 04/01/16.
 */
public class JsonDateSerializer extends JsonSerializer<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(LocalDateTime date, JsonGenerator generator, SerializerProvider provider)
            throws IOException {
        String dateString = date.format(formatter);
        generator.writeString(dateString);
    }
}