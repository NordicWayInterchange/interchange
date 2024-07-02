package no.vegvesen.ixn.napcore.model;

public record SubscriptionEndpoint(String host, Integer port, String source, Integer maxBandwidth, Integer maxMessageRate) {

    @Override
    public String toString() {
        return "SubscriptionEndpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
