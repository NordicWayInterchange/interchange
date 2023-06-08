package no.vegvesen.ixn.federation.qpid;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class QpidDelta {

    Set<Exchange> exchanges = new HashSet<>();

    Set<Queue> queues = new HashSet<>();

    public QpidDelta() {

    }

    public QpidDelta(Set<Exchange> exchanges, Set<Queue> queues) {
        this.exchanges = exchanges;
        this.queues = queues;
    }

    public Set<Exchange> getExchanges() {
        return exchanges;
    }

    public void setExchanges(Set<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    public Set<Queue> getQueues() {
        return queues;
    }

    public void setQueues(Set<Queue> queues) {
        this.queues = queues;
    }
}
