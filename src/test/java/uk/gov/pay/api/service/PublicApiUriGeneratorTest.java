package uk.gov.pay.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublicApiUriGeneratorTest {


    @Mock
    PublicApiConfig publicApiConfig;
    
    PublicApiUriGenerator publicApiUriGenerator;
    private String publicApiTestBaseUrl = "http://publicapi.test";
    
    @Before
    public void setUp() {
        when(publicApiConfig.getBaseUrl()).thenReturn(publicApiTestBaseUrl);
        publicApiUriGenerator = new PublicApiUriGenerator(publicApiConfig);
    }
    
    @Test
    public void convertHostToPublicAPI_with_http() {
        String originalHost = "http://something.test";
        String pathAndQuery = "/v1/api/events?queryParam1=value1&queryParam2=value2";
        assertEquals(publicApiTestBaseUrl + pathAndQuery, publicApiUriGenerator.convertHostToPublicAPI(originalHost + pathAndQuery) );
    }

    @Test
    public void convertHostToPublicAPI_with_https() {
        String originalHost = "https://something.test";
        String pathAndQuery = "/v1/api/events?queryParam1=value1&queryParam2=value2";
        assertEquals(publicApiTestBaseUrl + pathAndQuery, publicApiUriGenerator.convertHostToPublicAPI(originalHost + pathAndQuery) );
    }
}
