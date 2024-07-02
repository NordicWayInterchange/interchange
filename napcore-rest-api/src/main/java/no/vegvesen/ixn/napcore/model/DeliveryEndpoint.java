package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryEndpoint(String host, Integer port, String target, String selector, Integer maxBandwidth, Integer maxMessageRate) {

    @Override
    public String toString(){
        return "DeliveryEndpoint{" +
                "host='" + host + "'" +
                "port=" + port +
                "target='" + target + "'" +
                "selector='" + selector + "'" +
                "maxBandwidth=" + maxBandwidth +
                "maxMessageRate=" + maxMessageRate +
                "}";
    }
}
