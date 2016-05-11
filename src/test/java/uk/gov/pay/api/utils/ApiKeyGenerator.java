package uk.gov.pay.api.utils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.digest.HmacUtils;

public class ApiKeyGenerator {

    public static String apiKeyValueOf(String token, String secret) {
        byte[] hmacBytes = HmacUtils.hmacSha1(secret, token);
        String encodedHmac = BaseEncoding.base32Hex().lowerCase().omitPadding().encode(hmacBytes);
        return token + encodedHmac;
    }
}
