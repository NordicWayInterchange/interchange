package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Objects;

public class SubscriptionExchangeSubscriptionResponseApi {

    private String id;
    private String selector;
    private boolean createNewQueue; //optional, defaults to false
    private String queueConsumerUser; //optional, defaults to CN of requesting interchange
    private String path;
    private SubscriptionStatusApi status;

    public SubscriptionExchangeSubscriptionResponseApi() {
    }

    public SubscriptionExchangeSubscriptionResponseApi(String id, String selector, String path, SubscriptionStatusApi status) {
        this.id = id;
        this.selector = selector;
        this.path = path;
        this.status = status;
    }

    public SubscriptionExchangeSubscriptionResponseApi(String id, String selector, boolean createNewQueue, String queueConsumerUser, String path, SubscriptionStatusApi status) {
        this.id = id;
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.queueConsumerUser = queueConsumerUser;
        this.path = path;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public boolean isCreateNewQueue() {
        return createNewQueue;
    }

    public void setCreateNewQueue(boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
    }

    public String getQueueConsumerUser() {
        return queueConsumerUser;
    }

    public void setQueueConsumerUser(String queueConsumerUser) {
        this.queueConsumerUser = queueConsumerUser;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatusApi status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionExchangeSubscriptionResponseApi that = (SubscriptionExchangeSubscriptionResponseApi) o;
        return createNewQueue == that.createNewQueue &&
                Objects.equals(id, that.id) &&
                Objects.equals(selector, that.selector) &&
                Objects.equals(queueConsumerUser, that.queueConsumerUser) &&
                Objects.equals(path, that.path) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, selector, createNewQueue, queueConsumerUser, path, status);
    }

    @Override
    public String toString() {
        return "SubscriptionExchangeSubscriptionResponseApi{" +
                "id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", createNewQueue=" + createNewQueue +
                ", queueConsumerUser='" + queueConsumerUser + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                '}';
    }
}
