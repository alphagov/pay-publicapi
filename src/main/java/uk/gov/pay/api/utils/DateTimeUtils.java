package uk.gov.pay.api.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class DateTimeUtils {

    private static final ZoneId UTC = ZoneId.of("Z");
    private static DateTimeFormatter dateTimeFormatterUTC = DateTimeFormatter.ISO_INSTANT.withZone(UTC);
    private static DateTimeFormatter dateTimeFormatterAny = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public static Optional<ZonedDateTime> toUTCZonedDateTime(String aDate) {
        try {
            ZonedDateTime utcDateTime = ZonedDateTime
                    .parse(aDate, dateTimeFormatterAny)
                    .withZoneSameInstant(UTC);
            return Optional.of(utcDateTime);
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    public static String toUTCDateString(ZonedDateTime dateTime) {
        return dateTime.format(dateTimeFormatterUTC);
    }
}
