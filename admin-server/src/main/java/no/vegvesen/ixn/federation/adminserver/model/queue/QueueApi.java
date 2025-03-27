package no.vegvesen.ixn.federation.adminserver.model.queue;

public class QueueApi {
    private String id;

    private String name;

    private Boolean durable;

    private Long maximumMessageTtl;

    private Boolean ensureNondestructiveConsumers;

    public QueueApi() {

    }

    public QueueApi(String name, String id, Boolean durable, Long maximumMessageTtl, Boolean ensureNondestructiveConsumers) {
        this.name = name;
        this.id = id;
        this.durable = durable;
        this.maximumMessageTtl = maximumMessageTtl;
        this.ensureNondestructiveConsumers = ensureNondestructiveConsumers;
    }

    public String getName() {
        return name;
    }

    public Long getMaximumMessageTtl() {
        return maximumMessageTtl;
    }

    public String getId() {
        return id;
    }

    public Boolean getDurable() {
        return durable;
    }

    public Boolean getEnsureNondestructiveConsumers() {
        return ensureNondestructiveConsumers;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", durable=" + durable +
                ", maximumMessageTtl=" + maximumMessageTtl +
                ", ensureNondestructiveConsumers=" + ensureNondestructiveConsumers +
                '}';
    }
}
