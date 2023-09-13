package no.vegvesen.ixn.federation.qpid;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void addExchange(Exchange exchange) {
        this.exchanges.add(exchange);
    }

    public void removeExchange(String exchangeName) {
        findExchangeByName(exchangeName).ifPresent(value -> this.exchanges.remove(value));
    }

    public Set<Queue> getQueues() {
        return queues;
    }

    public void setQueues(Set<Queue> queues) {
        this.queues = queues;
    }

    public void addQueue(Queue queue) {
        this.queues.add(queue);
    }

    public void removeQueue(String queueName) {
        findQueueByName(queueName).ifPresent(value -> this.queues.remove(value));
    }

    public boolean exchangeExists(String exchangeName) {
        return exchanges.stream()
                .anyMatch(e -> e.getName().equals(exchangeName));
    }

    public boolean queueExists(String queueName) {
        return queues.stream()
                .anyMatch(q -> q.getName().equals(queueName));
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
                new Filter(selector),
                exchangeName
        )));
    }

    public boolean exchangeHasBindingToQueue(String exchangeName, String queueName) {
        return findExchangeByName(exchangeName)
                .map(value -> value.isBoundToQueue(queueName))
                .orElse(false);
    }
}
