package no.vegvesen.ixn.federation.qpid;

import java.util.Arrays;
import java.util.LinkedList;

public class QpidAcl {
    LinkedList<String> aclRules;


    public QpidAcl(String aclRules) {
        this.aclRules = new LinkedList<>(Arrays.asList(aclRules.split("\\r?\\n")));
    }

    public int size() {
        return aclRules.size();
    }

    public String get(int i) {
        return aclRules.get(i);
    }

    public void addQueueReadAccess(String memberOrGroupName, String queue) {
        String newAclEntry= String.format("ACL ALLOW-LOG %s CONSUME QUEUE name = \"%s\"", memberOrGroupName, queue);
        addNextToLast(newAclEntry);
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

    static String createQueueWriteAccessRule(String memberOrGroupName, String queue) {
        return String.format("ACL ALLOW-LOG %s PUBLISH EXCHANGE name = \"\" routingkey = \"%s\"", memberOrGroupName, queue);
    }

    static String createQeueReadAccessRule(String memberOrGroupName, String queue) {
        return String.format("ACL ALLOW-LOG %s CONSUME QUEUE name = \"%s\"", memberOrGroupName, queue);
    }


    private void addNextToLast(String rule) {
        aclRules.add(aclRules.size() - 1,rule);
    }

    public String aclAsString() {
        StringBuilder sb = new StringBuilder();
        for (String rule : aclRules) {
            sb.append(rule).append("\n");
        }
        return sb.toString();
    }

    public boolean containsRule(String rule) {
        AclRule aclRule = new AclRule(rule);
        for (String ruleTomatch : aclRules) {
            if (new AclRule(ruleTomatch).equals(aclRule)) {
                return true;
            }
        }
        return false;
    }
}
