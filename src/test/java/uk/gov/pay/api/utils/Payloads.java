package uk.gov.pay.api.utils;

public class Payloads {

    public static String aSuccessfulPaymentPayload() {
        int amount = 100;
        String returnUrl = "https://somewhere.gov.uk/rainbow/1";
        String reference = "a reference";
        String email = "alice.111@mail.fake";
        String description = "a description";
        return aSuccessfulPaymentPayload(amount, returnUrl, description, reference, email);
    }

    public static String aSuccessfulPaymentPayload(int amount, String returnUrl, String description, String reference, String email) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }
}
