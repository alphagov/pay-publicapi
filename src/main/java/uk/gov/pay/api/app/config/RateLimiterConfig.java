package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

public class RateLimiterConfig extends Configuration {

    @Min(1)
    private int noOfReq;

    @Min(1)
    private int noOfReqForPost;

    @Min(1)
    private int noOfReqForElevatedAccounts;

    @Min(1)
    private int noOfPostReqForElevatedAccounts;

    @Min(1)
    private int noOfReqPerNode;

    @Min(1)
    private int noOfReqForPostPerNode;

    @JsonDeserialize(converter = StringToListConverter.class)
    private List<String> elevatedAccounts;

    @Min(500)
    @Max(60000)
    private int perMillis;

    public int getNoOfReq() {
        return noOfReq;
    }

    public int getPerMillis() {
        return perMillis;
    }

    public int getNoOfReqForPost() {
        return noOfReqForPost;
    }

    public int getNoOfReqPerNode() {
        return noOfReqPerNode;
    }

    public int getNoOfReqForPostPerNode() {
        return noOfReqForPostPerNode;
    }

    public int getNoOfReqForElevatedAccounts() {
        return noOfReqForElevatedAccounts;
    }

    public int getNoOfPostReqForElevatedAccounts() {
        return noOfPostReqForElevatedAccounts;
    }

    public List<String> getElevatedAccounts() {
        return elevatedAccounts;
    }
}
