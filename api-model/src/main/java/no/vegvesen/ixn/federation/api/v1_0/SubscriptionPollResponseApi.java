package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionPollResponseApi {
    private String id;
    private String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean createNewQueue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueConsumerUser;

    private String path;
    private SubscriptionStatusApi status;

    private String messageBrokerUrl;

    private String queueName;

    private long lastUpdatedTimestamp;

    private String eTag;

    private Set<BrokerApi> brokers;

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

    public void setCreateNewQueue(Boolean createNewQueue) {
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

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Set<BrokerApi> getBrokers() {
        return brokers;
    }

    public void setBrokers(Set<BrokerApi> brokers) {
        this.brokers = brokers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPollResponseApi)) return false;
        SubscriptionPollResponseApi that = (SubscriptionPollResponseApi) o;
        return id.equals(that.id) &&
                selector.equals(that.selector) &&
                createNewQueue.equals(that.createNewQueue) &&
                queueConsumerUser.equals(that.queueConsumerUser) &&
                path.equals(that.path) &&
                status == that.status &&
                Objects.equals(messageBrokerUrl, that.messageBrokerUrl) &&
                Objects.equals(queueName, that.queueName) &&
                Objects.equals(lastUpdatedTimestamp, that.lastUpdatedTimestamp) &&
                Objects.equals(eTag, that.eTag) &&
                Objects.equals(brokers, that.brokers);
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
                queueName,
                lastUpdatedTimestamp,
                eTag,
                brokers);
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
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", eTag='" + eTag + '\'' +
                ", brokers=" + brokers +
                '}';
    }
}
