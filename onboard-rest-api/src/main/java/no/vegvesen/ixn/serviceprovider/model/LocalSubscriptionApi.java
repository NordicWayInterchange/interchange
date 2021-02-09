package no.vegvesen.ixn.serviceprovider.model;

public class LocalSubscriptionApi {
    private Integer id;
    private LocalSubscriptionStatusApi status;
    private String selector;
    private Boolean createNewQueue;

    public LocalSubscriptionApi() {
    }

    public LocalSubscriptionApi(Integer id, LocalSubscriptionStatusApi status, String selector) {
        this.id = id;
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscriptionApi(Integer id, LocalSubscriptionStatusApi status, String selector, Boolean createNewQueue) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.createNewQueue = createNewQueue;
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
}
