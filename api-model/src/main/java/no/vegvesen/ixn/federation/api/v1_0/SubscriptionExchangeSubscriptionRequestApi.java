package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

public class SubscriptionExchangeSubscriptionRequestApi {
    private String selector;

    @JsonInclude(Include.NON_NULL)
    private Boolean createNewQueue; //optional, thus an object

    @JsonInclude(Include.NON_NULL)
    private String queueConsumerUser;

    public SubscriptionExchangeSubscriptionRequestApi() {
    }

    public SubscriptionExchangeSubscriptionRequestApi(String selector) {
        this.selector = selector;
    }

    public SubscriptionExchangeSubscriptionRequestApi(String selector, Boolean createNewQueue, String queueConsumerUser) {
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.queueConsumerUser = queueConsumerUser;
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

    public String getQueueConsumerUser() {
        return queueConsumerUser;
    }

    public void setQueueConsumerUser(String queueConsumerUser) {
        this.queueConsumerUser = queueConsumerUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionExchangeSubscriptionRequestApi that = (SubscriptionExchangeSubscriptionRequestApi) o;
        return Objects.equals(selector, that.selector) &&
                Objects.equals(createNewQueue, that.createNewQueue) &&
                Objects.equals(queueConsumerUser, that.queueConsumerUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, createNewQueue, queueConsumerUser);
    }

    @Override
    public String toString() {
        return "SubscriptionExchangeSubscriptionRequestApi{" +
                "selector='" + selector + '\'' +
                ", createNewQueue=" + createNewQueue +
                ", queueConsumerUser='" + queueConsumerUser + '\'' +
                '}';
    }
}
