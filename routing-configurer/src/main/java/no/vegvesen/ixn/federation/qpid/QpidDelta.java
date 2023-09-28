package no.vegvesen.ixn.federation.qpid;

import java.util.*;
import java.util.stream.Collectors;

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

    public boolean exchangeExists(String exchangeName) {
        return exchanges.stream()
                .anyMatch(e -> e.getName().equals(exchangeName));
    }

    public boolean queueExists(String queueName) {
        return queues.stream()
                .anyMatch(q -> q.getName().equals(queueName));
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

    public Set<String> getDestinationsFromExchangeName(String exchangeName) {
        Set<String> result = new HashSet<>();
        if (findExchangeByName(exchangeName).isPresent()) {
            result = findExchangeByName(exchangeName).get().getBindings().stream()
                    .map(Binding::getDestination)
                    .collect(Collectors.toSet());
        }
        return result;
    }

    public void addBindingToExchange(String exchangeName, String selector, String destination) {
        findExchangeByName(exchangeName).ifPresent(ex -> ex.addBinding(new Binding(
                exchangeName,
                destination,
                new Filter(selector)
        )));
    }

    public boolean exchangeHasBindingToQueue(String exchangeName, String queueName) {
        return findExchangeByName(exchangeName)
                .map(value -> value.isBoundToQueue(queueName))
                .orElse(false);
    }

    public Exchange findByExchangeName(String exchangeName) {
        return findExchangeByName(exchangeName).orElse(null);
    }
}
