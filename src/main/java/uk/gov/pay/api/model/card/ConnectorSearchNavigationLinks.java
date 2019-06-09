package uk.gov.pay.api.model.card;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ConnectorSearchNavigationLinks {
    public ConnectorLink self;
    public ConnectorLink firstPage;
    public ConnectorLink lastPage;
    public ConnectorLink prevPage;
    public ConnectorLink nextPage;
}
