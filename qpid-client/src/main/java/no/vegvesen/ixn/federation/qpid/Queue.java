package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Queue {
    private String id;

    private String name;

    private Boolean durable;

    private Long maximumMessageTtl;

    public Queue() {

    }

    public Queue(String name, String id, Boolean durable, Long maximumMessageTtl) {
        this.name = name;
        this.id = id;
        this.durable = durable;
        this.maximumMessageTtl = maximumMessageTtl;
    }

    public Queue(String name) {
        this(name, null,null,null);
    }

    public Queue(String name, long maximumMessageTtl) {
        this(name,null,null,maximumMessageTtl);
    }

    public Queue(String name, String id) {
        this(name, id, null,null);
    }

    public Queue(String name, boolean durable) {
        this(name,null,durable,null);
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

    @Override
    public String toString() {
        return "Queue{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", durable=" + durable +
                ", maximumMessageTtl=" + maximumMessageTtl +
                '}';
    }

}
