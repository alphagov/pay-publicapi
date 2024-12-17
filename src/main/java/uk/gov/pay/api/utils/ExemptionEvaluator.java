package uk.gov.pay.api.utils;

import uk.gov.pay.api.model.Exemption;

import java.util.Optional;

public class ExemptionEvaluator {
    public static Exemption evaluateExemption(Exemption maybeExemption) {
        return Optional.ofNullable(maybeExemption)
                .map(exemption -> Optional.ofNullable(exemption.getType())
                        .map(type -> {
                            if (exemption.getOutcome() == null) {
                                return null;
                            }
                            switch (type) {
                                case "corporate" -> {
                                    return exemption;
                                }
                                default -> {
                                    return null;
                                }
                            }
                        })
                        .orElse(null))
                .orElse(null);
    }
}
