package uk.gov.pay.api.utils;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class DateTimeUtilsTest {

    @Test
    public void shouldConvertUTCZonedDateTimeToAISO_8601_UTCString() {
        ZonedDateTime localDateTime = ZonedDateTime.of(2010, 11, 13, 12, 0, 0, 999, ZoneId.of("Z"));

        String dateString = ISO_INSTANT_MILLISECOND_PRECISION.format(localDateTime);
        assertThat(dateString, is("2010-11-13T12:00:00.000Z"));
    }

    @Test
    public void shouldConvertUTCZonedDateTimeToLocalDateString() {
        ZonedDateTime localDateTime = ZonedDateTime.of(2010, 11, 13, 12, 0, 0, 0, ZoneId.of("Z"));

        String dateString = DateTimeUtils.toLocalDateString(localDateTime);
        assertThat(dateString, is("2010-11-13"));
    }

    @Test
    public void shouldConvertNonUTCZonedDateTimeToAISO_8601_UTCString() {
        ZonedDateTime localDateTime = ZonedDateTime.of(2010, 11, 13, 12, 0, 0, 999, ZoneId.of("Europe/Paris"));

        String dateString = ISO_INSTANT_MILLISECOND_PRECISION.format(localDateTime);
        assertThat(dateString, is("2010-11-13T11:00:00.000Z"));
    }

    @Test
    public void shouldConvertUTCZonedISO_8601StringToADateTime() {
        String aDate = "2010-01-01T12:00:00Z";
        Optional<ZonedDateTime> result = DateTimeUtils.toUTCZonedDateTime(aDate);
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), endsWith("Z"));

        aDate = "2010-12-31T23:59:59.132Z";
        result = DateTimeUtils.toUTCZonedDateTime(aDate);
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), endsWith("Z"));
    }

    @Test
    public void shouldConvertNonUTCZonedISO_8601StringToADateTime() {
        String aDate = "2010-01-01T12:00:00+01:00[Europe/Paris]";
        Optional<ZonedDateTime> result = DateTimeUtils.toUTCZonedDateTime(aDate);
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), endsWith("Z"));

        aDate = "2010-12-31T23:59:59.132+01:00[Europe/Paris]";
        result = DateTimeUtils.toUTCZonedDateTime(aDate);
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), endsWith("Z"));
    }
}
