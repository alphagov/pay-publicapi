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

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class DirectDebitEventsPagination {
        @JsonProperty("self") 
        private Link selfLink;
        @JsonProperty("first_page") 
        private Link firstLink;
        @JsonProperty("last_page") 
        private Link lastLink;
        @JsonProperty("prev_page") 
        private Link prevLink;
        @JsonProperty("next_page") 
        private Link nextLink;
        
        @JsonSetter("self")
        void setSelfLink(Link link) {
            this.selfLink = link;
        }

        @JsonSetter("first_page")
        void setFirstLink(Link link) {
            this.firstLink = link;
        }
        
        @JsonSetter("last_page")
        void setLastLink(Link link) {
            this.lastLink = link;
        }

        @JsonSetter("prev_page")
        void setPrevLink(Link link) {
            this.prevLink = link;
        }

        @JsonSetter("next_page")
        void setNextLink(Link link) {
            this.nextLink = link;
        }
        

        @JsonFormat(shape = JsonFormat.Shape.OBJECT)
        private static class Link {
            
            @JsonProperty("href")
            private String href;
            
            @JsonSetter("href")
            void setHref(String link) {
                this.href = convertLink(link);
            }

            private String convertLink(String link) {
                return link.replace("mandate", "agreement");
            }
        }
    }
}
