package uk.gov.pay.api.resources;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.api.model.ExternalChargeStatus;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ApiValidator {

    private final List<Pair<String, String>> invalidParams;

    public static ApiValidator queryParamValidator() {
        return new ApiValidator();
    }

    public Optional<List<Pair<String, String>>> build() {
        if (invalidParams.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(invalidParams);
    }

    public ApiValidator validateDates(List<Pair<String, String>> queryParams) {

        queryParams.stream()
                .forEach(param -> {
                    String dateString = param.getRight();
                    if (isNotBlank(dateString) && !DateTimeUtils.toUTCZonedDateTime(dateString).isPresent()) {
                        invalidParams.add(param);
                    }
                });

        return this;
    }

    public ApiValidator validateStatus(Pair<String, String> status) {
        String statusString = status.getRight();
        if (isNotBlank(statusString) && !ExternalChargeStatus.mapFromStatus(statusString).isPresent()) {
            invalidParams.add(status);
        }
        return this;
    }

    private ApiValidator() {
        this.invalidParams = new ArrayList<>();
    }
}
