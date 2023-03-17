package no.vegvesen.ixn.federation.qpid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class QpidAcl {
    LinkedList<AclRule> aclRules;


    public QpidAcl(LinkedList<AclRule> rules) {
        this.aclRules = rules;
    }

    public int size() {
        return aclRules.size();
    }

    public AclRule get(int i) {
        return aclRules.get(i);
    }

    public void addQueueReadAccess(String memberOrGroupName, String queue) {
        addNextToLast(createQeueReadAccessRule(memberOrGroupName,queue));
    }

   public void addQueueWriteAccess(String memberOrGroupName, String queue) {
        addNextToLast(createQueueWriteAccessRule(memberOrGroupName,queue));
    }

    public boolean removeQueueReadAccess(String memberOrGroupName, String queue) {
        return aclRules.remove(createQeueReadAccessRule(memberOrGroupName,queue));
    }

    public boolean removeQueueWriteAccess(String memberOrGroupName, String queue) {
        return aclRules.remove(createQueueWriteAccessRule(memberOrGroupName,queue));
    }

    public void createPublishAccessOnExchangeForQueue(String memberName, String queue) {
        addNextToLast(createQueuePublishToExchangeRule(memberName, queue));
    }

    public void createConsumeAccessOnQueueForExchange(String queue) {
        addNextToLast(createExchangeConsumeOnQueueRule(queue));
    }

    /**
     * NOTE this acl is a bit strange, as we are allowing to publish to an exchange, not queue, with routingKey = queue name.
     * This is a qpid workaround since it has not been possible to write to queues in AMQP 0.X
     * @param memberOrGroupName
     * @param queue
     */
    static AclRule createQueueWriteAccessRule(String memberOrGroupName, String queue) {
        Map<String,String> props = new HashMap<>();
        props.put("routingkey",queue);
        props.put("name","");
        return new AclRule("ALLOW-LOG", memberOrGroupName,"PUBLISH","EXCHANGE",props);
    }

    static AclRule createQeueReadAccessRule(String memberOrGroupName, String queue) {
        Map<String, String> props = new HashMap<>();
        props.put("name",queue);
        return new AclRule("ALLOW-LOG",memberOrGroupName,"CONSUME","QUEUE",props);
    }

    static AclRule createQueuePublishToExchangeRule(String memberName, String queue) {
        Map<String, String> props = new HashMap<>();
        props.put("routingkey", queue);
        props.put("name", "");
        return new AclRule("ALLOW-LOG", memberName, "PUBLISH", "EXCHANGE", props);
    }

    static AclRule createExchangeConsumeOnQueueRule(String queue) {
        Map<String, String> props = new HashMap<>();
        props.put("name", queue);
        return new AclRule("ALLOW-LOG", "interchange", "CONSUME", "QUEUE", props);
    }

    public static QpidAcl parseRules(String aclRules) {
        return new QpidAcl(new LinkedList<>(Arrays.asList(createRules(aclRules))));
    }


    private void addNextToLast(AclRule rule) {
        aclRules.add(aclRules.size() - 1,rule);
    }

    public String aclAsString() {
        StringBuilder sb = new StringBuilder();
        for (AclRule rule : aclRules) {
            sb.append(rule.toRuleString()).append("\n");
        }
        return sb.toString();
    }

    public boolean containsRule(AclRule rule) {
        return aclRules.contains(rule);
    }

    private static AclRule[] createRules(String rules) {
        String[] ruleLines = rules.split("\\r?\\n");
        AclRule[] newRules = new AclRule[ruleLines.length];
        for (int i = 0; i < ruleLines.length; i++) {
            newRules[i] = AclRule.parse(ruleLines[i]);
        }
        return newRules;

    }
}
