package uk.gov.pay.api.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.DirectDebitEventService;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DirectDebitEventsResourceTest {

    @Mock
    private ConnectorUriGenerator connectorUriGenerator;

    @Mock
    private DirectDebitEventService directDebitEventService;

    private DirectDebitEventsResource directDebitEventsResource;

    @BeforeEach
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
