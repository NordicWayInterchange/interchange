package no.vegvesen.ixn.federation.api.v1_0.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;

import java.util.Objects;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "version",
        defaultImpl = SubscriptionPollResponseApiV1.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubscriptionPollResponseApiV1.class, name = ApiVersion.VERSION_1_2),
        @JsonSubTypes.Type(value = SubscriptionPollResponseApiV2.class, name = ApiVersion.VERSION_2_0)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SubscriptionPollResponseApi {

    private String version;

    private String id;

    private String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String consumerCommonName;

    private String path;
    private SubscriptionStatusApi status;


    private long lastUpdatedTimestamp;


    public SubscriptionPollResponseApi() {
    }

    public SubscriptionPollResponseApi(String version,
                                       String id,
                                       String selector,
                                       String path,
                                       SubscriptionStatusApi status,
                                       String consumerCommonName,
                                       long lastUpdatedTimestamp) {
        this.version = version;
        this.id = id;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.path = path;
        this.status = status;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
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

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPollResponseApi)) return false;
        SubscriptionPollResponseApi that = (SubscriptionPollResponseApi) o;
        return id.equals(that.id) &&
                selector.equals(that.selector) &&
                consumerCommonName.equals(that.consumerCommonName) &&
                path.equals(that.path) &&
                status == that.status &&
                Objects.equals(lastUpdatedTimestamp, that.lastUpdatedTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                selector,
                consumerCommonName,
                path,
                status,
                lastUpdatedTimestamp);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApi{" +
                "id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
