package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.pay.api.validation.URL;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreatePaymentRequest {

    private Integer amount;
    private String returnUrl;
    private String reference;
    private String description;

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty("return_url")
    @URL
    @Size(max = 2000)
    @NotBlank
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    @JsonProperty
    @NotNull
    @Min(1)
    @Max(10000000)
    public Integer getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "payment reference", required = true, example = "12345")
    @JsonProperty
    @NotBlank
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "payment description", required = true, example = "New passport application")
    @JsonProperty
    @NotBlank
    public String getDescription() {
        return description;
    }
}
