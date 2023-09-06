package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Queue {

    String name;

    private boolean durable = true;

    //TODO this default might have to change.... Find out where this is set in the code.
    private long maximumMessageTtl = 0l;


    public Queue() {

    }

    public Queue(String name) {
        this.name = name;
    }

    public Queue(String name, long maximumMessageTtl) {
        this.name = name;
        this.maximumMessageTtl = maximumMessageTtl;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "name='" + name + '\'' +
                ", maximumMessageTtl=" + maximumMessageTtl +
                ", durable=" + durable +
                '}';
    }

    public long getMaximumMessageTtl() {
        return maximumMessageTtl;
    }

    public boolean isDurable() {
        return durable;
    }
}
