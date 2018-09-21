package uk.gov.pay.api.validation;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.eclipse.jetty.util.StringUtil.isBlank;

class NumericValidator {
    static boolean isValid(String value) {
        return isBlank(value) || isNumeric(value);
    }
}
