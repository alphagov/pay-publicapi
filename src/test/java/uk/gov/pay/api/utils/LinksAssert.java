package uk.gov.pay.api.utils;

import com.jayway.restassured.response.ValidatableResponse;

import static javax.ws.rs.HttpMethod.GET;
import static org.hamcrest.Matchers.is;

public class LinksAssert {
    public static void assertLink(ValidatableResponse response, String paymentUrl, String linkRel) {
        response.body("links.find {links -> links.rel == '" + linkRel + "' }.href", is(paymentUrl));
        response.body("links.find {links -> links.rel == '" + linkRel + "' }.method", is(GET));
    }
}
