package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;

public class LocalEndpointExportApi {

    private String host;

    private Integer port;

    private String source;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    public LocalEndpointExportApi() {

    }

    public LocalEndpointExportApi(String host,
                                  Integer port,
                                  String source,
                                  Integer maxBandwidth,
                                  Integer maxMessageRate) {
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
        LocalEndpointExportApi that = (LocalEndpointExportApi) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(source, that.source) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, source, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "LocalEndpointExportApi{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
