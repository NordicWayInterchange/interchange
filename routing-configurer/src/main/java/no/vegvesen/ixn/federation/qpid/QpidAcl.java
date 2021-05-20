package no.vegvesen.ixn.federation.qpid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class QpidAcl {
    LinkedList<AclRule> aclRules;


    public QpidAcl(String aclRules) {
        this.aclRules = new LinkedList<>(Arrays.asList(createRules(aclRules.split("\\r?\\n"))));
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

    /**
     * NOTE this acl is a bit strange, as we are allowing to publis to an exchange, not queue, with routingKey = queue name.
     * This is a qpid workaround since it has not been possible to write to queues in AMQP 0.X
     * @param memberOrGroupName
     * @param queue
     */
    public void addQueueWriteAccess(String memberOrGroupName, String queue) {
        addNextToLast(createQueueWriteAccessRule(memberOrGroupName,queue));
    }

    public boolean removeQueueReadAccess(String memberOrGroupName, String queue) {
        return aclRules.remove(createQeueReadAccessRule(memberOrGroupName,queue));
    }

    public boolean removeQueueWriteAccess(String memberOrGroupName, String queue) {
        return aclRules.remove(createQueueWriteAccessRule(memberOrGroupName,queue));
    }

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

    private AclRule[] createRules(String[] rules) {
        AclRule[] newRules = new AclRule[rules.length];
        for (int i = 0; i < rules.length; i++) {
            newRules[i] = new AclRule(rules[i]);
        }
        return newRules;

    }
}
