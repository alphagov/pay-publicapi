package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@ApiModel(value = "Billing Address", description = "A structure representing the billing address of a card")
public class Address {

    private String line1;
    private String line2;
    private String postcode;
    private String city;
    private String country;

    public Address(@JsonProperty("line1") String line1,
                   @JsonProperty("line2") String line2,
                   @JsonProperty("postcode") String postcode,
                   @JsonProperty("city") String city,
                   @JsonProperty("country") String country) {
        this.line1 = line1;
        this.line2 = line2;
        this.postcode = postcode;
        this.city = city;
        this.country = country;
    }

    @ApiModelProperty(example = "address line 1")
    public String getLine1() {
        return line1;
    }

    @ApiModelProperty(example = "address line 2")
    public String getLine2() {
        return line2;
    }

    @ApiModelProperty(example = "AB1 2CD")
    public String getPostcode() {
        return postcode;
    }

    @ApiModelProperty(example = "address city")
    public String getCity() {
        return city;
    }

    @ApiModelProperty(example = "UK")
    public String getCountry() {
        return country;
    }
}
