package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualHostAccessController {

    private String id;

    private String name;

    private List<AclRule> rules;

    public VirtualHostAccessController() {
    }

    public VirtualHostAccessController(String id, List<AclRule> rules) {
        this.id = id;
        this.rules = rules;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static AclRule createQueueReadAccessRule(String memberOrGroupName, String queueName) {
        Map<String, String> props = new HashMap<>();
        props.put("NAME", queueName);
        return new AclRule(memberOrGroupName,"CONSUME","ALLOW_LOG","QUEUE",props);
    }

    public static AclRule createExchangeWriteAccessRule(String memberOrGroupName, String queue)  {
        Map<String,String> props = new HashMap<>();
        props.put("ROUTING_KEY",queue);
        props.put("NAME","");
        return new AclRule(memberOrGroupName,"PUBLISH","ALLOW_LOG","EXCHANGE",props);
    }

    public String getId() {
        return id;
    }

    public List<AclRule> getRules() {
        return rules;
    }

    public void addQueueReadAccess(String subscriberName, String queue) {
        rules.add(rules.size() - 1, createQueueReadAccessRule(subscriberName,queue));
    }

    public void addExchangeWriteAccess(String subscriberName, String queue) {
        rules.add(rules.size() -1, createExchangeWriteAccessRule(subscriberName,queue));
    }

    public void removeQueueReadAccess(String subscriberName, String queue) {
        rules.remove(createQueueReadAccessRule(subscriberName,queue));
    }

    public void removeQueueWriteAccess(String subscriberName, String queue) {
        rules.remove(createExchangeWriteAccessRule(subscriberName,queue));
    }

    public boolean containsRule(AclRule rule) {
        return rules.contains(rule);

    }

    protected void addRule(AclRule rule) {
        rules.add(rule);
    }

    @Override
    public String toString() {
        return "VirtualHostAccessControlProvider{" +
                "id='" + id + '\'' +
                ", rules=" + rules +
                '}';
    }
}
