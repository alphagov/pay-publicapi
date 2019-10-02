package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(name = "Payer")
public class Payer {

    @JsonProperty("name")
    @Schema(accessMode = READ_ONLY)
    private String name;

    @JsonProperty("email")
    @Schema(accessMode = READ_ONLY)
    private String email;

    public Payer() {
    }

    public Payer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
