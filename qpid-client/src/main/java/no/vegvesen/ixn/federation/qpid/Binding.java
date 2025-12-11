package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Binding {

    String bindingKey;

    String destination;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Filter arguments;


    public Binding() {
    }

    public Binding(String bindingKey, String destination, Filter arguments) {
        this.bindingKey = bindingKey;
        this.destination = destination;
        this.arguments = arguments;
    }

    public String getBindingKey() {
        return bindingKey;
    }


    public String getDestination() {
        return destination;
    }


    public Filter getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "bindingKey='" + bindingKey + '\'' +
                ", destination='" + destination + '\'' +
                ", arguments=" + arguments + '\'' +
                '}';
    }
}
