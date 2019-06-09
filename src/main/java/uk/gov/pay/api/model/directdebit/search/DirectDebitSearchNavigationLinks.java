package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DirectDebitSearchNavigationLinks {
    public DirectDebitConnectorLink self;
    public DirectDebitConnectorLink firstPage;
    public DirectDebitConnectorLink lastPage;
    public DirectDebitConnectorLink prevPage;
    public DirectDebitConnectorLink nextPage;

}
