package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

@ApiModel(value="UnauthorizedResponse", description = "Unauthorized Response")
@JsonSnakeCase
public class Unauthorized401Response {

    private final String message;

    private Unauthorized401Response(String message) {
        this.message = message;
    }

    public static Unauthorized401Response createUnauthorized401Response(String message) {
        return new Unauthorized401Response(message);
    }

    @ApiModelProperty(value = "message", required = true)
    @JsonProperty
    @NotBlank
    public String getMessage() {
        return message;
    }
}