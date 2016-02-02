package uk.gov.pay.api.resources;

import uk.gov.pay.api.utils.DateTimeUtils;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

public class ApiValidators {

    public static Optional<String> validateQueryParams(String fromDate, String toDate) {
        List<String> invalidQueryParams = newArrayList();
        if (isNotBlank(fromDate) && !DateTimeUtils.toUTCZonedDateTime(fromDate).isPresent()) {
            invalidQueryParams.add(PaymentsResource.FROM_DATE_KEY);
        }
        if (isNotBlank(fromDate) && !DateTimeUtils.toUTCZonedDateTime(toDate).isPresent()) {
            invalidQueryParams.add(PaymentsResource.TO_DATE_KEY);
        }
        if (invalidQueryParams.size() > 0) {
            return Optional.of(format("fields [%s] are not in correct format. see public api documentation for the correct data formats",
                    join(invalidQueryParams, ", ")));
        }
        return Optional.empty();
    }
}
