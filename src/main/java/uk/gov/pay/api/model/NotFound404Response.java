package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

@ApiModel(value="NotFoundResponse", description = "Not Found Response")
@JsonSnakeCase
public class NotFound404Response {

    private final String message;

    private NotFound404Response(String message) {
        this.message = message;
    }

    public static NotFound404Response createNotFound404Response(String message) {
        return new NotFound404Response(message);
    }

    @ApiModelProperty(value = "message", required = true)
    @JsonProperty
    @NotBlank
    public String getMessage() {
        return message;
    }
}