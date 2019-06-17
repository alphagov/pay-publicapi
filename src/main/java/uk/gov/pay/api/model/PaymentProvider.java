package uk.gov.pay.api.model;

public enum PaymentProvider {
    
    GOCARDLESS("gocardless");

    private String name;

    PaymentProvider(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
