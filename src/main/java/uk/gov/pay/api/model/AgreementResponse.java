package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.utils.CustomSupportedLanguageDeserializer;
import uk.gov.service.payments.commons.api.json.ExternalMetadataDeserialiser;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AgreementResponse {
    private String externalId;
    private String serviceId;
    private String reference;
    private String description;
    private String status;
    private String createdDate;
    private PaymentInstrument paymentInstrument;
    
    @JsonProperty("external_id")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @JsonProperty("id")
    public String getExternalId() {
        return externalId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public PaymentInstrument getPaymentInstrument() {
        return paymentInstrument;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class PaymentInstrument {
        private String externalId;
        private String agreementExternalId;
        private CardDetails cardDetails;
        private String createdDate;

        public String getExternalId() {
            return externalId;
        }

        public String getAgreementExternalId() {
            return agreementExternalId;
        }

        public CardDetails getCardDetails() {
            return cardDetails;
        }

        public String getCreatedDate() {
            return createdDate;
        }
    }
}
