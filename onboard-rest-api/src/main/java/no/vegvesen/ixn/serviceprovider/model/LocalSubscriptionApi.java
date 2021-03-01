package no.vegvesen.ixn.serviceprovider.model;

public class LocalSubscriptionApi {
    private Integer id;
    private LocalSubscriptionStatusApi status;
    private String selector;
    private Boolean createNewQueue;
    private String brokerUrl;

    public LocalSubscriptionApi() {
    }

    public LocalSubscriptionApi(Integer id, LocalSubscriptionStatusApi status, String selector) {
        this.id = id;
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscriptionApi(Integer id, LocalSubscriptionStatusApi status, String selector, Boolean createNewQueue, String brokerUrl) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.brokerUrl = brokerUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusApi status) {
        this.status = status;
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

    public String getBrokerUrl(){
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
}
