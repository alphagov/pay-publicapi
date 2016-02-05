package uk.gov.pay.api.resources;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.api.model.ExternalChargeStatus;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.pay.api.resources.PaymentsResource.*;

public class ApiValidators {

    public static Optional<String> validateQueryParams(List<Pair<String, String>> queryParams) {
        List<String> invalidQueryParams = newArrayList();

        queryParams.stream()
                .filter(param -> FROM_DATE_KEY.equals(param.getLeft()) || TO_DATE_KEY.equals(param.getLeft()))
                .forEach(param -> {
                    String dateString = param.getRight();
                    if (isNotBlank(dateString) && !DateTimeUtils.toUTCZonedDateTime(dateString).isPresent()) {
                        invalidQueryParams.add(param.getLeft());
                    }
                });

        queryParams.stream()
                .filter(param -> STATUS_KEY.equals(param.getLeft()))
                .forEach(param -> {
                    String statusString = param.getRight();
                    if (isNotBlank(statusString)  && !ExternalChargeStatus.mapFromStatus(statusString).isPresent()) {
                        invalidQueryParams.add(param.getLeft());
                    }
                });

        if (invalidQueryParams.size() > 0) {
            return Optional.of(format("fields [%s] are not in correct format. see public api documentation for the correct data formats",
                    join(invalidQueryParams, ", ")));
        }
        return Optional.empty();
    }
}
