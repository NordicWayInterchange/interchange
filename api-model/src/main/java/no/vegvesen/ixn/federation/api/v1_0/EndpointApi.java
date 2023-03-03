package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointApi {

    private String source;

    private String host;
    private Integer port;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxBandwidth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxMessageRate;

    public EndpointApi() {

    }

    public EndpointApi(String source, String host, Integer port) {
        this.source = source;
        this.host = host;
        this.port = port;
    }

    public EndpointApi(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate) {
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
        if (!(o instanceof EndpointApi)) return false;
        EndpointApi that = (EndpointApi) o;
        return source.equals(that.source) &&
                host.equals(that.host) &&
                port.equals(that.port) &&
                Objects.equals(maxBandwidth, that.maxBandwidth) &&
                Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, host, port, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "EndpointApi{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
