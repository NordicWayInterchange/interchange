package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class LocalEndpoint {
    private String host;
    private int port;
    private String source;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalEndpoint() {
    }

    public LocalEndpoint(String host, int port, String source) {
        this.host = host;
        this.port = port;
        this.source = source;
    }

    public LocalEndpoint(String host, int port, String source, Integer maxBandwidth, Integer maxMessageRate) {
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
        return Objects.equals(host, localEndpoint.host) && Objects.equals(port, localEndpoint.port) &&Objects.equals(source, localEndpoint.source) && Objects.equals(maxBandwidth, localEndpoint.maxBandwidth) && Objects.equals(maxMessageRate, localEndpoint.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, source, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "url='" + host + '\'' +
                ", port='" + port + '\'' +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
