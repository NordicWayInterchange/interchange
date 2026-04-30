package no.vegvesen.ixn.federation.adminserver.model.exchange;


import no.vegvesen.ixn.federation.adminserver.qpid.Binding;

import java.util.ArrayList;
import java.util.List;

public class ExchangeApi {
    private static final String DEFAULT_TYPE = "headers";
    private static final boolean DEFAULT_DURABILITY = true;

    private final String id;
    private final String name;

    private final boolean durable;

    private final String type;

    List<Binding> bindings;

    private AlternateBinding alternateBinding;

    public ExchangeApi() {
        this(null, null, DEFAULT_DURABILITY, DEFAULT_TYPE, new ArrayList<>(), null);
    }

    public ExchangeApi(String name, String id, boolean durable, String type, List<Binding> bindings, AlternateBinding alternateBinding) {
        this.name = name;
        this.id = id;
        this.durable = durable;
        this.type = type;
        this.bindings = new ArrayList<>();
        this.bindings.addAll(bindings);
        this.alternateBinding = alternateBinding;
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
