package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Exchange {

    private static final String DEFAULT_TYPE = "headers";
    private static final boolean DEFAULT_DURABILITY = true;

    private String id;
    private String name;

    private boolean durable;

    private String type;

    List<Binding> bindings;

    private AlternateBinding alternateBinding;

    public Exchange() {
        this(null, null, DEFAULT_DURABILITY, DEFAULT_TYPE, new ArrayList<>(), null);
    }

    public Exchange(String name, String id, boolean durable, String type, List<Binding> bindings, AlternateBinding alternateBinding) {
        this.name = name;
        this.id = id;
        this.durable = durable;
        this.type = type;
        this.bindings = new ArrayList<>();
        this.bindings.addAll(bindings);
        this.alternateBinding = alternateBinding;
    }

    public Exchange(String name) {
        this(name, null, DEFAULT_DURABILITY, DEFAULT_TYPE, new ArrayList<>(), null);
    }

    public Exchange(String name, List<Binding> bindings) {
        this(name, null, DEFAULT_DURABILITY, DEFAULT_TYPE, bindings, null);
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


    public boolean isBoundTo(String destinationName) {
        return bindings.stream()
                .anyMatch(binding -> binding.getDestination().equals(destinationName));
    }


    public boolean isDurable() {
        return durable;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public AlternateBinding getAlternateBinding() {
        return alternateBinding;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", durable=" + durable +
                ", type='" + type + '\'' +
                ", bindings=" + bindings +
                ", alternateBinding=" + (alternateBinding != null ? alternateBinding.destination() : "null") +
                '}';
    }
}
