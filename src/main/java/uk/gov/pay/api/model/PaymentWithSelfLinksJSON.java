package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A Payment with only a self link")
public interface PaymentWithSelfLinksJSON {

    String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    @ApiModelProperty(dataType = "uk.gov.pay.api.model.SelfLinks")
    SelfLinks getLinks();

}
