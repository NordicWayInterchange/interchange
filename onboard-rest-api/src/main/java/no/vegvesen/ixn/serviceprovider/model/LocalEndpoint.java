package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class LocalEndpoint {
    //private String url;
    private String host;
    private Integer port;
    private String source;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalEndpoint() {
    }

    public LocalEndpoint(String host, Integer port, String source) {
        this.host = host;
        this.port = port;
        this.source = source;
    }

    public LocalEndpoint(String host, Integer port, String source, Integer maxBandwidth, Integer maxMessageRate) {
        this.host = host;
        this.port = port;
        this.source = source;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
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

    //TODO: Remove temporary method for Url, probably don't need it for testing purposes
    public String getUrl() {
        if(port == null){
            return String.format("amqps://%s:%s", host, "5671");
        }
        else {
            return String.format("amqps://%s:%s", host, port);
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public Integer getMaxMessageRate() {
        return maxMessageRate;
    }

    public void setMaxMessageRate(Integer maxMessageRate) {
        this.maxMessageRate = maxMessageRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalEndpoint localEndpoint = (LocalEndpoint) o;
        return Objects.equals(host, localEndpoint.host) && Objects.equals(port, localEndpoint.port) && Objects.equals(source, localEndpoint.source) && Objects.equals(maxBandwidth, localEndpoint.maxBandwidth) && Objects.equals(maxMessageRate, localEndpoint.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, source, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
