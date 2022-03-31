package uk.gov.pay.api.utils.mocks;

import java.util.List;
import java.util.Objects;

public class CreateAgreementRequestParams {
    private final String reference;

    private CreateAgreementRequestParams(CreateAgreementRequestParamsBuilder builder) {
        this.reference = builder.reference;
    }

    public String getReference() {
        return reference;
    }

    public static final class CreateAgreementRequestParamsBuilder {
        private String reference;

        private CreateAgreementRequestParamsBuilder() {
        }

        public static CreateAgreementRequestParamsBuilder aCreateAgreementRequestParams() {
            return new CreateAgreementRequestParamsBuilder();
        }

        public CreateAgreementRequestParamsBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public CreateAgreementRequestParams build() {
            List.of(reference).forEach(Objects::requireNonNull);
            return new CreateAgreementRequestParams(this);
        }
    }
}
