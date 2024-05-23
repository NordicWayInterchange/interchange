package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class NeighbourEndpointExportApi {

    private String source;

    private String host;

    private Integer port;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    public NeighbourEndpointExportApi() {

    }

    public NeighbourEndpointExportApi(String source,
                                      String host,
                                      Integer port,
                                      Integer maxBandwidth,
                                      Integer maxMessageRate) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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
        NeighbourEndpointExportApi that = (NeighbourEndpointExportApi) o;
        return Objects.equals(source, that.source) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, host, port, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "NeighbourEndpointExportApi{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
