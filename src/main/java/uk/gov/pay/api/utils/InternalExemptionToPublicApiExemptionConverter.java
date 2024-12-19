package uk.gov.pay.api.utils;

import uk.gov.pay.api.model.Exemption;

import java.util.Optional;

public class InternalExemptionToPublicApiExemptionConverter {
    public static Exemption convertExemption(Exemption maybeExemption) {
        return Optional.ofNullable(maybeExemption)
                .map(exemption -> {
                    if (exemption.getType() != null
                            && exemption.getType().equals("corporate")
                            && exemption.getOutcome() != null) {
                        return exemption;
                    }
                    return null;
                })
                .orElse(null);
    }
}
