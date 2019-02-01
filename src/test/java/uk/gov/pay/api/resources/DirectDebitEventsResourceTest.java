package uk.gov.pay.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.DirectDebitEventService;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class DirectDebitEventsResourceTest {

    @Mock
    private ConnectorUriGenerator connectorUriGenerator;

    @Mock
    private DirectDebitEventService directDebitEventService;

    private DirectDebitEventsResource directDebitEventsResource;


    @Before
    public void setUp() {
        directDebitEventsResource = new DirectDebitEventsResource(connectorUriGenerator, directDebitEventService);
    }

    @Test
    public void testAllNullValues() {
        directDebitEventsResource.getDirectDebitEvents(null, null, null, null, null, null, null);
        verify(connectorUriGenerator, times(1)).eventsURI(null, Optional.empty(), Optional.empty(), null, null, null, null);
    }

    @Test
    public void testWithADate() {
        ZonedDateTime toDate = ZonedDateTime.now();
        directDebitEventsResource.getDirectDebitEvents(null, toDate.toString(), null, null, null, null, null);
        verify(connectorUriGenerator, times(1)).eventsURI(null, Optional.of(toDate), Optional.empty(), null, null, null, null);
    }

}
