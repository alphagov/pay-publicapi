package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitEventsResponse {

    @JsonProperty("page")
    private int page;

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("results")
    private List<DirectDebitEvent> results;

    @JsonProperty("_links")
    private DirectDebitEventsPagination links;

    public static class DirectDebitEventsPagination {
        @JsonProperty("self") 
        private String selfLink;
        @JsonProperty("first_page") 
        private String firstLink;
        @JsonProperty("last_page") 
        private String lastLink;
        @JsonProperty("prev_page") 
        private String prevLink;
        @JsonProperty("next_page") 
        private String nextLink;
        
        @JsonSetter("self")
        void setSelfLink(String link) {
            this.selfLink = convertLink(link);
        }

        @JsonSetter("first_page")
        void setFirstLink(String link) {
            this.firstLink = convertLink(link);
        }
        
        @JsonSetter("last_page")
        void setLastLink(String link) {
            this.lastLink = convertLink(link);
        }

        @JsonSetter("prev_page")
        void setPrevLink(String link) {
            this.prevLink = convertLink(link);
        }

        @JsonSetter("next_page")
        void setnextLink(String link) {
            this.nextLink = convertLink(link);
        }
        
        private String convertLink(String link) {
            return link.replace("mandate", "agreement");
        }
    }
}
