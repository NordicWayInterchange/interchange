package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Exchange {

    String name;

    Set<Binding> bindings;

    public Exchange() {
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

    @Override
    public String toString() {
        return "Exchange{" +
                "name='" + name + '\'' +
                ", bindings=" + bindings +
                '}';
    }
}
