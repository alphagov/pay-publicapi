package uk.gov.pay.api.utils;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class PathHelperTest {

    @Test
    @Parameters({
            "/v1/payments,POST,create_payment",
            "/v1/payments/,POST,create_payment",
            "/v1/payments/paymentId/capture,POST,capture_payment",
            "/v1/payments/paymentId/capture/,POST,capture_payment",
            "/v1/payments/paymentId/cancel,POST,",
            "/v1/payments,GET,"
    })
    public void returnsPathType(String path, String method, String pathType) {
        assertThat(PathHelper.getPathType(path, method), is(pathType));
    }
}
