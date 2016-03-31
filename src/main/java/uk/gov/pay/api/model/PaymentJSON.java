package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A Payment")
interface PaymentJSON {

    @JsonProperty("created_date")
    @ApiModelProperty(example = "2016-01-21T17:15:00Z")
    String getCreatedDate();

    @JsonProperty("payment_id")
    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    String getPaymentId();

    @JsonProperty("amount")
    @ApiModelProperty(example = "1200")
    long getAmount();

    @JsonProperty("status")
    @ApiModelProperty(example = "CREATED")
    String getStatus();

    @JsonProperty("return_url")
    @ApiModelProperty(example = "http://your.service.domain/your-reference")
    String getReturnUrl();

    @JsonProperty("description")
    @ApiModelProperty(example = "Your Service Description")
    String getDescription();

    @JsonProperty("reference")
    @ApiModelProperty(example = "your-reference")
    String getReference();

    @JsonProperty("payment_provider")
    @ApiModelProperty(example = "worldpay")
    String getPaymentProvider();
}
