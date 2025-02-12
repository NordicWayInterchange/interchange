package no.vegvesen.ixn.federation.qpid;

import java.util.*;

public class QpidDelta {

    List<Exchange> exchanges = new ArrayList<>();

    List<Queue> queues = new ArrayList<>();

    public QpidDelta(List<Exchange> exchanges, List<Queue> queues) {
        this.exchanges.addAll(exchanges);
        this.queues.addAll(queues);

    }

    public void addExchange(Exchange exchange) {
        this.exchanges.add(exchange);
    }

    public void removeExchange(Exchange exchange) {
        exchanges.remove(exchange);
    }

    public void addQueue(Queue queue) {
        this.queues.add(queue);
    }

    public void removeQueue(Queue queue) {
        queues.remove(queue);
    }

    public Queue findByQueueName(String queueName) {
        return findQueueByName(queueName).orElse(null);
    }

    public Optional<Queue> findQueueByName(String queueName) {
        return queues.stream()
                .filter(e -> e.getName().equals(queueName))
                .findFirst();
    }

    public Optional<Exchange> findExchangeByName(String exchangeName) {
        return exchanges.stream()
                .filter(e -> e.getName().equals(exchangeName))
                .findFirst();
    }

    public Exchange findByExchangeName(String exchangeName) {
        return findExchangeByName(exchangeName).orElse(null);
    }
}
