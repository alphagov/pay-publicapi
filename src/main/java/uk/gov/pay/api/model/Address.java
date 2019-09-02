package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.validation.ExactLengthOrEmpty;

import javax.validation.constraints.Size;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@ApiModel(value = "Address", description = "A structure representing the billing address of a card")
public class Address {

    @Size(max = 255, message = "Must be less than or equal to {max} characters length")
    private String line1;

    @Size(max = 255, message = "Must be less than or equal to {max} characters length")
    private String line2;

    @Size(max = 25, message = "Must be less than or equal to {max} characters length")
    private String postcode;

    @Size(max = 255, message = "Must be less than or equal to {max} characters length")
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

    @ApiModelProperty(example = "GB")
    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(line1, address.line1) &&
                Objects.equals(line2, address.line2) &&
                Objects.equals(postcode, address.postcode) &&
                Objects.equals(city, address.city) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, postcode, city, country);
    }
}
