package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Exchange {

    private String name;

    private boolean durable = true;

    private String type = "headers";

    Set<Binding> bindings = new HashSet<>();

    public Exchange() {
    }

    public Exchange(String name) {
        this.name = name;
    }

    public Exchange(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Exchange(String name, Set<Binding> bindings) {
        this.name = name;
        this.bindings = bindings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Binding> getBindings() {
        return bindings;
    }

    public void setBindings(Set<Binding> bindings) {
        this.bindings = bindings;
    }

    public void addBinding(Binding binding) {
        this.bindings.add(binding);
    }


    public boolean isBoundToQueue(String queueName) {
        return bindings.stream()
                .anyMatch(q -> q.getDestination().equals(queueName));
    }


    @Override
    public String toString() {
        return "Exchange{" +
                "name='" + name + '\'' +
                ", bindings=" + bindings +
                '}';
    }

    public boolean isDurable() {
        return durable;
    }

    public String getType() {
        return type;
    }
}
