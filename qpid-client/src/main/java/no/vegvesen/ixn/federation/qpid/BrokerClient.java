package no.vegvesen.ixn.federation.qpid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BrokerClient {
    boolean queueExists(String queueName);

    void removeQueue(String queueName);

    boolean exchangeExists(String capabilityExchangeName);

    void bindDirectExchange(String selector, String exchange, String queueName);

    void bindTopicExchange(String selector, String exchange, String commonName);

    void createQueue(String queueName);

    void addReadAccess(String subscriberName, String queueName);

    List<String> getGroupMemberNames(String groupName);

    void addMemberToGroup(String subscriberName, String groupName);

    void removeMemberFromGroup(String subscriberName, String groupName);

    void removeReadAccess(String name, String source);

    void addWriteAccess(String peerName, String queueName);

    void removeWriteAccess(String name, String queueName);

    Set<String> getQueueBindKeys(String source);

    void unbindBindKey(String source, String bindKey, String exchangeName);

    void removeExchange(String exchangeName);

    void createTopicExchange(String capabilityExchangeName);

    void bindToBiQueue(String capabilitySelector, String capabilityExchangeName);

    void createDirectExchange(String exchangeName);
}
