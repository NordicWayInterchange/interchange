package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Binding {

    String bindingKey;

    String destination;

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
