package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

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

    @ApiModelProperty(value = "service return url", required = true)
    @JsonProperty
    @NotBlank
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "account id", required = false)
    @JsonProperty
    public String getAccountId() {
        return accountId;
    }

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = ">0")
    @JsonProperty
    @NotNull
    public Long getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "payment reference", required = true)
    @JsonProperty
    @NotBlank
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "payment description", required = true)
    @JsonProperty
    @NotBlank
    public String getDescription() {
        return description;
    }
}