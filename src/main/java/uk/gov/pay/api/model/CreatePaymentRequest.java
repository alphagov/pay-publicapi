package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel(value="CreatePaymentRequest", description = "The Payment Request Payload")
@JsonSnakeCase
public class CreatePaymentRequest {

    @JsonProperty("account_id")
    private String accountId;

    private Long amount;
    @JsonProperty("return_url")
    private String returnUrl;
    private String reference;
    private String description;

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty
    @NotBlank
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "account id", required = false, example = "789")
    @JsonProperty
    public String getAccountId() {
        return accountId;
    }

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = ">0", example = "12000")
    @JsonProperty
    @NotNull
    @Min(1)
    @Max(10000000)
    public Long getAmount() {
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