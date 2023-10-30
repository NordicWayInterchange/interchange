package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class PrivateChannelEndpoint {
    private String host;
    private int port;
    private String target;
    public PrivateChannelEndpoint() {
    }

    public PrivateChannelEndpoint(String host, int port, String target) {
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelEndpoint that = (PrivateChannelEndpoint) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(target, that.target);
    }
    @Override
    public int hashCode(){
        return Objects.hash(host, port, target);
    }
    @Override
    public String toString(){
        return "";
    }
}
