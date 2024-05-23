package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class PrivateChannelEndpointExportApi {

    private String host;

    private Integer port;

    private String queueName;

    public PrivateChannelEndpointExportApi() {

    }

    public PrivateChannelEndpointExportApi(String host,
                                           Integer port,
                                           String queueName) {
        this.host = host;
        this.port = port;
        this.queueName = queueName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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
        PrivateChannelEndpointExportApi that = (PrivateChannelEndpointExportApi) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(queueName, that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, queueName);
    }

    @Override
    public String toString() {
        return "PrivateChannelEndpointExportApi{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
