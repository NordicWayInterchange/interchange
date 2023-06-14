package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Exchange {

    String name;

    Set<Binding> bindings = new HashSet<>();

    public Exchange() {
    }

    public Exchange(String name) {
        this.name = name;
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

    public void removeBinding(String name, String destination) {
        findBindingByNameAndDestination(name, destination).ifPresent(value -> this.bindings.remove(value));
    }

    public Optional<Binding> findBindingByNameAndDestination(String name, String destination) {
        return bindings.stream()
                .filter(binding -> binding.getName().equals(name) && binding.getDestination().equals(destination))
                .findFirst();
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
}
