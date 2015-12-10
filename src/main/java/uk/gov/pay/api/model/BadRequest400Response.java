package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

@ApiModel(value="BadRequestResponse", description = "Bad Request Response")
@JsonSnakeCase
public class BadRequest400Response {

    private final String message;

    private BadRequest400Response(String message) {
        this.message = message;
    }

    public static BadRequest400Response createBadRequest400Response(String message) {
        return new BadRequest400Response(message);
    }

    @ApiModelProperty(value = "message", required = true)
    @JsonProperty
    @NotBlank
    public String getMessage() {
        return message;
    }
}