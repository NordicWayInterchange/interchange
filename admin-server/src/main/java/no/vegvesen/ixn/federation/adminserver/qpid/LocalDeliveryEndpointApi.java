package no.vegvesen.ixn.federation.adminserver.qpid;

public class LocalDeliveryEndpointApi {

    private String host;

    private Integer port;

    private String target;

    public LocalDeliveryEndpointApi() {}

    public LocalDeliveryEndpointApi(String host, Integer port, String target) {
        this.host = host;
        this.port = port;
        this.target = target;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    @Override
    public String toString() {
        return "QpidDeliveryEndpointApi{" +
                "host='" + host + "'" +
                "port=" + port +
                "target='" + target +
                "}";
    }
}
