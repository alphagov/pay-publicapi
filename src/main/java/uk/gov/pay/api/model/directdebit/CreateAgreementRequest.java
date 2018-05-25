package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateAgreementRequest", description = "The Payload to create a new Agreement")
public class CreateAgreementRequest {
    private String name;
    private String email;
    private AgreementType type;

    @ApiModelProperty(value = "agreement name", required = true, example = "Service Name agreement")
    @JsonProperty(value = "name")
    public String getName() {
        return this.name;
    }

    @ApiModelProperty(value = "email", required = true, example = "email@example.com")
    @JsonProperty(value = "email")
    public String getEmail() {
        return this.email;
    }

    @ApiModelProperty(value = "agreement type", required = true, example = "on demand")
    @JsonProperty(value = "type")
    public AgreementType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "AgreementRequestCreate{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                '}';
    }
}
