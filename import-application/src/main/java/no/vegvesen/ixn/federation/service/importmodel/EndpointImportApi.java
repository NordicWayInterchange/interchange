package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;

public class EndpointImportApi {

    private String source;

    private String host;

    private Integer port;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private String dlq;

    public EndpointImportApi() {

    }

    public EndpointImportApi(String source,
                             String host,
                             Integer port,
                             Integer maxBandwidth,
                             Integer maxMessageRate,
                             String dlq) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.dlq = dlq;
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

    public String getDlq() {
        return dlq;
    }

    public void setDlq(String dlq) {
        this.dlq = dlq;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EndpointImportApi that = (EndpointImportApi) o;
        return Objects.equals(source, that.source) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate) && Objects.equals(dlq, that.dlq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, host, port, maxBandwidth, maxMessageRate, dlq);
    }

    @Override
    public String toString() {
        return "EndpointImportApi{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", dlq='" + dlq + '\'' +
                '}';
    }
}
