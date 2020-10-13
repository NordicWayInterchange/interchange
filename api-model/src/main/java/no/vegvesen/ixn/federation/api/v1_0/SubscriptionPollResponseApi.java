package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class SubscriptionPollResponseApi {
    private String id;
    private String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean createNewQueue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueConsumerUser;
    private String path;
    private SubscriptionStatusApi status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String messageBrokerUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueName;

    public SubscriptionPollResponseApi() {
    }

    public SubscriptionPollResponseApi(String id,
                                       String selector,
                                       String path,
                                       SubscriptionStatusApi status,
                                       String messageBrokerUrl,
                                       String queueName,
                                       Boolean createNewQueue,
                                       String queueConsumerUser) {
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

    public Boolean isCreateNewQueue() {
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
        SubscriptionPollResponseApi that = (SubscriptionPollResponseApi) o;
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
        return "SubscriptionPollResponseApi{" +
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
