package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class PrivateChannelEndpointApi {
    private String host;
    private Integer port;
    private String queueName;
    public PrivateChannelEndpointApi() {
    }

    public PrivateChannelEndpointApi(String host, Integer port, String target) {
        this.host = host;
        this.port = port;
        this.queueName = target;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelEndpointApi that = (PrivateChannelEndpointApi) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(queueName, that.queueName);
    }
    @Override
    public int hashCode(){
        return Objects.hash(host, port, queueName);
    }
    @Override
    public String toString(){
        return "";
    }
}
