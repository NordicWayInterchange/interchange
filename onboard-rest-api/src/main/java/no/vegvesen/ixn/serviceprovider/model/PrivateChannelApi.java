package no.vegvesen.ixn.serviceprovider.model;

public class PrivateChannelApi {

    private Integer id;
    private String clientName;
    private String queueName;

    public PrivateChannelApi() {

    }

    public PrivateChannelApi(String clientName) {
        this.clientName = clientName;
    }

    public PrivateChannelApi(String clientName, String queueName, Integer id) {
        this.id = id;
        this.clientName = clientName;
        this.queueName = queueName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
