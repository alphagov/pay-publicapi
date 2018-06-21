package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.Address;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DDPayer {
    private String name;
    private String email;
    @JsonProperty("account_last_two_digits")
    private String accountLast2Digits;
    private Address address;

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getAccountLast2Digits() { return accountLast2Digits; }

    public Address getAddress() { return address; }
}
