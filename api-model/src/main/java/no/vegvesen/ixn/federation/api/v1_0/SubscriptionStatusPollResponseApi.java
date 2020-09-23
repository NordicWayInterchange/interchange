package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Objects;

public class SubscriptionStatusPollResponseApi {
    private String id;
    private String selector;
    private boolean createNewQueue;
    private String queueConsumerUser;
    private String path;
    private SubscriptionStatusApi status;
    private String messageBrokerUrl;
    private String queueName;

    public SubscriptionStatusPollResponseApi() {
    }

    public SubscriptionStatusPollResponseApi(String id,
                                             String selector,
                                             boolean createNewQueue,
                                             String queueConsumerUser,
                                             String path,
                                             SubscriptionStatusApi status,
                                             String messageBrokerUrl,
                                             String queueName) {
        this.id = id;
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.queueConsumerUser = queueConsumerUser;
        this.path = path;
        this.status = status;
        this.messageBrokerUrl = messageBrokerUrl;
        this.queueName = queueName;
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

    public String getMessageBrokerUrl() {
        return messageBrokerUrl;
    }

    public void setMessageBrokerUrl(String messageBrokerUrl) {
        this.messageBrokerUrl = messageBrokerUrl;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionStatusPollResponseApi that = (SubscriptionStatusPollResponseApi) o;
        return createNewQueue == that.createNewQueue &&
                Objects.equals(id, that.id) &&
                Objects.equals(selector, that.selector) &&
                Objects.equals(queueConsumerUser, that.queueConsumerUser) &&
                Objects.equals(path, that.path) &&
                status == that.status &&
                Objects.equals(messageBrokerUrl, that.messageBrokerUrl) &&
                Objects.equals(queueName, that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                selector,
                createNewQueue,
                queueConsumerUser,
                path,
                status,
                messageBrokerUrl,
                queueName);
    }

    @Override
    public String toString() {
        return "SubscriptionStatusPollResponseApi{" +
                "id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", createNewQueue=" + createNewQueue +
                ", queueConsumerUser='" + queueConsumerUser + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", messageBrokerUrl='" + messageBrokerUrl + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
