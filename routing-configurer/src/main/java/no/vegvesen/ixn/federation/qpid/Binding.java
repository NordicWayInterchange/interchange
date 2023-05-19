package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Binding {

    String bindingKey;

    String destination;

    Filter arguments;

    String name;

    public Binding() {
    }

    public Binding(String bindingKey, String destination, Filter arguments, String name) {
        this.bindingKey = bindingKey;
        this.destination = destination;
        this.arguments = arguments;
        this.name = name;
    }

    public String getBindingKey() {
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Filter getArguments() {
        return arguments;
    }

    public void setArguments(Filter arguments) {
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "bindingKey='" + bindingKey + '\'' +
                ", destination='" + destination + '\'' +
                ", arguments=" + arguments +
                ", name='" + name + '\'' +
                '}';
    }
}
