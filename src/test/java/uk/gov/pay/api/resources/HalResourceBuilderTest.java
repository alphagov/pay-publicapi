package uk.gov.pay.api.resources;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class HalResourceBuilderTest {

    @Test
    public void shouldGetChargeTransactionHal() throws Exception {
        String result = new HalResourceBuilder(uriOf("/self"))
                .withProperty("count", 100)
                .withProperty("total", 300)
                .withLink("first_page", uriOf("?page=1"))
                .withLink("next_page", uriOf("?page=3"))
                .withLink("last_page", uriOf("?page=5"))
                .withLink("previous_page", uriOf("?page=2"))
                .withProperty("results", ImmutableList.of("one", "two"))
                .build();

        assertEquals("hal response mismatch",
                "{" +
                        "\"total\":300," +
                        "\"count\":100," +
                        "\"results\":[\"one\",\"two\"]," +
                        "\"_links\":" +
                        "{\"next_page\":{\"href\":\"?page=3\"}," +
                        "\"self\":{\"href\":\"/self\"}," +
                        "\"previous_page\":{\"href\":\"?page=2\"}," +
                        "\"last_page\":{\"href\":\"?page=5\"}," +
                        "\"first_page\":{\"href\":\"?page=1\"}" +
                        "}" +
                        "}",
                result);
    }

    private URI uriOf(String link) throws URISyntaxException {
        return new URI(link);
    }

}