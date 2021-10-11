package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestedSubscriptionApi {
    private String selector;

    @JsonInclude(Include.NON_NULL)
    private Boolean createNewQueue; //optional, thus an object

    @JsonInclude(Include.NON_NULL)
    private String consumerCommonName;

    public RequestedSubscriptionApi() {
    }

    public RequestedSubscriptionApi(String selector) {
        this.selector = selector;
    }

    public RequestedSubscriptionApi(String selector, Boolean createNewQueue, String consumerCommonName) {
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.consumerCommonName = consumerCommonName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Boolean getCreateNewQueue() {
        return createNewQueue;
    }

    public void setCreateNewQueue(Boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedSubscriptionApi that = (RequestedSubscriptionApi) o;
        return Objects.equals(selector, that.selector) &&
                Objects.equals(createNewQueue, that.createNewQueue) &&
                Objects.equals(consumerCommonName, that.consumerCommonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, createNewQueue, consumerCommonName);
    }

    @Override
    public String toString() {
        return "RequestedSubscriptionApi{" +
                "selector='" + selector + '\'' +
                ", createNewQueue=" + createNewQueue +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                '}';
    }
}
