package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointApiV2 {

    private String source;

    private String host;
    private Integer port;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean requiresDynamicFilter;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxBandwidth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxMessageRate;

    public EndpointApiV2() {

    }

    public EndpointApiV2(String source, String host, Integer port, Boolean requiresDynamicFilter) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.requiresDynamicFilter = requiresDynamicFilter;
    }

    public EndpointApiV2(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate, Boolean requiresDynamicFilter) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.requiresDynamicFilter = requiresDynamicFilter;
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

    public Boolean getRequiresDynamicFilter() {
        return requiresDynamicFilter;
    }

    public void setRequiresDynamicFilter(Boolean requiresDynamicFilter) {
        this.requiresDynamicFilter = requiresDynamicFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointApiV2)) return false;
        EndpointApiV2 that = (EndpointApiV2) o;
        return source.equals(that.source) &&
                host.equals(that.host) &&
                port.equals(that.port) &&
                Objects.equals(maxBandwidth, that.maxBandwidth) &&
                Objects.equals(maxMessageRate, that.maxMessageRate) &&
                requiresDynamicFilter == that.requiresDynamicFilter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, host, port, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "EndpointApiV2{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", requiresDynamicFilter=" + requiresDynamicFilter +
                '}';
    }
}
