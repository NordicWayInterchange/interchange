package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Exchange {

    private String name;

    private boolean durable = true;

    private String type = "headers";

    List<Binding> bindings = new ArrayList<>();

    public Exchange() {
    }

    public Exchange(String name) {
        this.name = name;
    }

    public Exchange(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Exchange(String name, List<Binding> bindings) {
        this.name = name;
        this.bindings.addAll(bindings);
    }

    public String getName() {
        return name;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public void addBinding(Binding binding) {
        this.bindings.add(binding);
    }


    public boolean isBoundToQueue(String queueName) {
        return bindings.stream()
                .anyMatch(q -> q.getDestination().equals(queueName));
    }


    public boolean isDurable() {
        return durable;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return null;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "name='" + name + '\'' +
                ", durable=" + durable +
                ", type='" + type + '\'' +
                ", bindings=" + bindings +
                '}';
    }
}
