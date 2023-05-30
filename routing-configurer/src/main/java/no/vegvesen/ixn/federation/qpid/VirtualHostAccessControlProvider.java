package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualHostAccessControlProvider {

    private String id;

    private String name;

    private List<NewAclRule> rules;

    public VirtualHostAccessControlProvider() {
    }

    public VirtualHostAccessControlProvider(String id, List<NewAclRule> rules) {
        this.id = id;
        this.rules = rules;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRules(List<NewAclRule> rules) {
        this.rules = rules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static NewAclRule createQueueReadAccessRule(String memberOrGroupName, String queueName) {
        Map<String, String> props = new HashMap<>();
        props.put("NAME", queueName);
        return new NewAclRule(memberOrGroupName,"ACCESS","ALLOW_LOG","QUEUE",props);
    }

    public static NewAclRule createQueueWriteAccessRule(String memberOrGroupName, String queue)  {
        Map<String,String> props = new HashMap<>();
        props.put("ROUTING_KEY",queue);
        props.put("NAME","");
        return new NewAclRule(memberOrGroupName,"PUBLISH","ALLOW_LOG","EXCHANGE",props);
    }

    public static NewAclRule createQueuePublishToExchangeRule(String memberName, String queue) {
        Map<String, String> props = new HashMap<>();
        props.put("routingkey", queue);
        props.put("NAME", "");
        return new NewAclRule(memberName,"PUBLISH","ALLOW-LOG","EXCHANGE",props);
    }

    public static NewAclRule createExchangeConsumeOnQueueRule(String queue) {
        Map<String, String> props = new HashMap<>();
        props.put("NAME", queue);
        return new NewAclRule("interchange","CONSUME","ALLOW_LOG","QUEUE",props);

    }

    public String getId() {
        return id;
    }

    public List<NewAclRule> getRules() {
        return rules;
    }

    public void addQueueReadAccess(String subscriberName, String queue) {
        rules.add(rules.size() - 1, createQueueReadAccessRule(subscriberName,queue));
    }

    public void addQueueWriteAccess(String subscriberName, String queue) {
        rules.add(rules.size() -1, createQueueWriteAccessRule(subscriberName,queue));
    }

    public void removeQueueReadAccess(String subscriberName, String queue) {
        rules.remove(createQueueReadAccessRule(subscriberName,queue));
    }

    public void removeQueueWriteAccess(String subscriberName, String queue) {
        rules.remove(createQueueWriteAccessRule(subscriberName,queue));
    }

    public boolean containsRule(NewAclRule rule) {
        return rules.contains(rule);

    }

    @Override
    public String toString() {
        return "VirtualHostAccessControlProvider{" +
                "id='" + id + '\'' +
                ", rules=" + rules +
                '}';
    }
}
